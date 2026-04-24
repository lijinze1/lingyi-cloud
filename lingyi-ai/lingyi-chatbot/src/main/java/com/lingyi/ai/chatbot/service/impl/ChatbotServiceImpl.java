package com.lingyi.ai.chatbot.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.ai.chatbot.config.ChatDefaultsProperties;
import com.lingyi.ai.chatbot.dto.ChatMessageSendRequest;
import com.lingyi.ai.chatbot.dto.ChatSessionCreateRequest;
import com.lingyi.ai.chatbot.entity.LyChatMessage;
import com.lingyi.ai.chatbot.entity.LyChatMessageRef;
import com.lingyi.ai.chatbot.entity.LyChatSession;
import com.lingyi.ai.chatbot.entity.LyChatSessionContext;
import com.lingyi.ai.chatbot.mapper.ChatMessageMapper;
import com.lingyi.ai.chatbot.mapper.ChatMessageRefMapper;
import com.lingyi.ai.chatbot.mapper.ChatSessionContextMapper;
import com.lingyi.ai.chatbot.mapper.ChatSessionMapper;
import com.lingyi.ai.chatbot.service.AiOptionResolver;
import com.lingyi.ai.chatbot.service.ChatbotService;
import com.lingyi.ai.chatbot.service.PromptRuntimeService;
import com.lingyi.ai.chatbot.service.ProductContextProvider;
import com.lingyi.ai.chatbot.vo.ChatMessageVO;
import com.lingyi.ai.chatbot.vo.ChatReplyVO;
import com.lingyi.ai.chatbot.vo.ChatSessionVO;
import com.lingyi.common.ai.AiModelConfig;
import com.lingyi.common.ai.PublishedPromptSnapshot;
import com.lingyi.common.util.SnowflakeIdGenerator;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final String BIZ_TYPE = "chatbot";
    private static final int CONTEXT_WINDOW_SIZE = 12;
    private static final int SUMMARY_TRIGGER_SIZE = 18;

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionContextMapper chatSessionContextMapper;
    private final ChatMessageRefMapper chatMessageRefMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final ChatModel chatModel;
    private final ChatDefaultsProperties defaultsProperties;
    private final PromptRuntimeService promptRuntimeService;
    private final AiOptionResolver aiOptionResolver;
    private final ProductContextProvider productContextProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatbotServiceImpl(ChatSessionMapper chatSessionMapper,
                              ChatMessageMapper chatMessageMapper,
                              ChatSessionContextMapper chatSessionContextMapper,
                              ChatMessageRefMapper chatMessageRefMapper,
                              SnowflakeIdGenerator idGenerator,
                              ChatModel chatModel,
                              ChatDefaultsProperties defaultsProperties,
                              PromptRuntimeService promptRuntimeService,
                              AiOptionResolver aiOptionResolver,
                              ProductContextProvider productContextProvider) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.chatSessionContextMapper = chatSessionContextMapper;
        this.chatMessageRefMapper = chatMessageRefMapper;
        this.idGenerator = idGenerator;
        this.chatModel = chatModel;
        this.defaultsProperties = defaultsProperties;
        this.promptRuntimeService = promptRuntimeService;
        this.aiOptionResolver = aiOptionResolver;
        this.productContextProvider = productContextProvider;
    }

    @Override
    public ChatSessionVO createSession(ChatSessionCreateRequest request) {
        LyChatSession session = new LyChatSession();
        session.setId(idGenerator.nextId());
        session.setUserId(request.getUserId());
        session.setBizRefId(request.getBizRefId());
        session.setBizType(BIZ_TYPE);
        session.setTitle(request.getTitle());
        session.setStatus(1);
        session.setIsDeleted(0);
        chatSessionMapper.insert(session);
        ensureSessionContext(session.getId());
        return toSessionVO(session);
    }

    @Override
    public List<ChatSessionVO> sessions(Long userId) {
        return chatSessionMapper.selectList(new LambdaQueryWrapper<LyChatSession>()
                        .eq(LyChatSession::getIsDeleted, 0)
                        .eq(LyChatSession::getBizType, BIZ_TYPE)
                        .eq(userId != null, LyChatSession::getUserId, userId)
                        .orderByDesc(LyChatSession::getCreatedAt))
                .stream()
                .map(this::toSessionVO)
                .toList();
    }

    @Override
    public List<ChatMessageVO> messages(Long sessionId) {
        requireSession(sessionId);
        return chatMessageMapper.selectList(new LambdaQueryWrapper<LyChatMessage>()
                        .eq(LyChatMessage::getSessionId, sessionId)
                        .eq(LyChatMessage::getIsDeleted, 0)
                        .orderByAsc(LyChatMessage::getCreatedAt))
                .stream()
                .map(this::toMessageVO)
                .toList();
    }

    @Override
    @Transactional
    public ChatReplyVO send(ChatMessageSendRequest request) {
        LyChatSession session = requireSession(request.getSessionId());
        ensureConfig();
        LyChatSessionContext contextState = ensureSessionContext(session.getId());
        PublishedPromptSnapshot promptSnapshot = promptRuntimeService.loadPublishedPrompt(defaultsProperties.getPromptCode());
        AiModelConfig modelConfig = aiOptionResolver.resolve(promptSnapshot.getModelConfig());

        LyChatMessage userMessage = new LyChatMessage();
        userMessage.setId(idGenerator.nextId());
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(request.getContent().trim());
        userMessage.setTokensIn(Math.max(1, request.getContent().length() / 4));
        userMessage.setModel(modelConfig.getModel());
        userMessage.setIsDeleted(0);
        chatMessageMapper.insert(userMessage);

        String context = productContextProvider.buildContext(session.getBizRefId(), request.getUserId(), request.getContent());
        refreshSummaryIfNeeded(session.getId(), contextState);
        String replyContent = callChatModel(session.getId(), request.getContent(), context, contextState, promptSnapshot, modelConfig);

        LyChatMessage assistantMessage = new LyChatMessage();
        assistantMessage.setId(idGenerator.nextId());
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(replyContent);
        assistantMessage.setTokensOut(Math.max(1, replyContent.length() / 4));
        assistantMessage.setModel(modelConfig.getModel());
        assistantMessage.setIsDeleted(0);
        chatMessageMapper.insert(assistantMessage);

        persistMessageRef(assistantMessage.getId(), "prompt", promptSnapshot.getPromptCode(), promptSnapshot.getPromptVersionId(),
                Map.of("promptId", promptSnapshot.getPromptId(), "promptVersionId", promptSnapshot.getPromptVersionId()));
        if (context != null && !context.isBlank()) {
            persistMessageRef(assistantMessage.getId(), "product", "product-context", session.getBizRefId(),
                    Map.of("contentLength", context.length()));
        }
        contextState.setLastMessageId(assistantMessage.getId());
        chatSessionContextMapper.updateById(contextState);
        refreshSummaryIfNeeded(session.getId(), contextState);

        if (session.getTitle() == null || session.getTitle().isBlank()) {
            session.setTitle(buildTitle(request.getContent()));
            chatSessionMapper.updateById(session);
        }

        ChatReplyVO reply = new ChatReplyVO();
        reply.setSession(toSessionVO(session));
        reply.setAssistantMessage(toMessageVO(assistantMessage));
        return reply;
    }

    private String callChatModel(Long sessionId,
                                 String userContent,
                                 String productContext,
                                 LyChatSessionContext contextState,
                                 PublishedPromptSnapshot promptSnapshot,
                                 AiModelConfig modelConfig) {
        List<Message> messages = new ArrayList<>();
        String systemPrompt = promptRuntimeService.renderContent(promptSnapshot, Map.of(
                "productContext", safeText(productContext),
                "conversationSummary", safeText(contextState.getSummary())));
        messages.add(new SystemMessage(systemPrompt));
        if (contextState.getSummary() != null && !contextState.getSummary().isBlank()) {
            messages.add(new SystemMessage("历史会话摘要: " + contextState.getSummary()));
        }
        if (productContext != null && !productContext.isBlank()) {
            messages.add(new SystemMessage("商品上下文: " + productContext));
        }
        List<LyChatMessage> history = chatMessageMapper.selectList(new LambdaQueryWrapper<LyChatMessage>()
                .eq(LyChatMessage::getSessionId, sessionId)
                .eq(LyChatMessage::getIsDeleted, 0)
                .gt(contextState.getLastContextMessageId() != null, LyChatMessage::getId, contextState.getLastContextMessageId())
                .orderByAsc(LyChatMessage::getCreatedAt));
        List<LyChatMessage> recentHistory = trimToWindow(history);
        for (LyChatMessage historyMessage : recentHistory) {
            if ("assistant".equals(historyMessage.getRole())) {
                messages.add(new AssistantMessage(historyMessage.getContent()));
            } else {
                messages.add(new UserMessage(historyMessage.getContent()));
            }
        }
        messages.add(new UserMessage(userContent));
        Prompt prompt = new Prompt(messages, buildOptions(modelConfig));
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    private DashScopeChatOptions buildOptions(AiModelConfig modelConfig) {
        DashScopeChatOptions.DashscopeChatOptionsBuilder builder = DashScopeChatOptions.builder();
        if (!isBlank(modelConfig.getModel())) {
            builder.withModel(modelConfig.getModel());
        }
        if (modelConfig.getTemperature() != null) {
            builder.withTemperature(modelConfig.getTemperature().doubleValue());
        }
        if (modelConfig.getTopP() != null) {
            builder.withTopP(modelConfig.getTopP().doubleValue());
        }
        if (modelConfig.getTopK() != null) {
            builder.withTopK(modelConfig.getTopK());
        }
        if (modelConfig.getMaxTokens() != null) {
            builder.withMaxToken(modelConfig.getMaxTokens());
        }
        Double repetitionPenalty = modelConfig.getFrequencyPenalty() != null
                ? modelConfig.getFrequencyPenalty().doubleValue()
                : modelConfig.getPresencePenalty() != null ? modelConfig.getPresencePenalty().doubleValue() : null;
        if (repetitionPenalty != null) {
            builder.withRepetitionPenalty(repetitionPenalty);
        }
        return builder.build();
    }

    private void refreshSummaryIfNeeded(Long sessionId, LyChatSessionContext contextState) {
        List<LyChatMessage> unsummarized = chatMessageMapper.selectList(new LambdaQueryWrapper<LyChatMessage>()
                .eq(LyChatMessage::getSessionId, sessionId)
                .eq(LyChatMessage::getIsDeleted, 0)
                .gt(contextState.getLastContextMessageId() != null, LyChatMessage::getId, contextState.getLastContextMessageId())
                .orderByAsc(LyChatMessage::getCreatedAt));
        if (unsummarized.size() <= SUMMARY_TRIGGER_SIZE) {
            if (!unsummarized.isEmpty()) {
                contextState.setLastMessageId(unsummarized.get(unsummarized.size() - 1).getId());
                chatSessionContextMapper.updateById(contextState);
            }
            return;
        }
        List<LyChatMessage> toSummarize = unsummarized.subList(0, unsummarized.size() - CONTEXT_WINDOW_SIZE);
        String summary = summarizeMessages(contextState.getSummary(), toSummarize);
        contextState.setSummary(summary);
        contextState.setSummaryVersion(contextState.getSummaryVersion() == null ? 1 : contextState.getSummaryVersion() + 1);
        contextState.setLastContextMessageId(toSummarize.get(toSummarize.size() - 1).getId());
        contextState.setLastMessageId(unsummarized.get(unsummarized.size() - 1).getId());
        chatSessionContextMapper.updateById(contextState);
    }

    private String summarizeMessages(String existingSummary, List<LyChatMessage> messages) {
        List<String> lines = new ArrayList<>();
        if (existingSummary != null && !existingSummary.isBlank()) {
            lines.add(existingSummary);
        }
        lines.addAll(messages.stream()
                .map(message -> message.getRole() + ":" + shrink(message.getContent(), 180))
                .collect(Collectors.toList()));
        String summary = String.join(" | ", lines);
        return shrink(summary, 2000);
    }

    private List<LyChatMessage> trimToWindow(List<LyChatMessage> messages) {
        if (messages.size() <= CONTEXT_WINDOW_SIZE) {
            return messages;
        }
        return messages.subList(messages.size() - CONTEXT_WINDOW_SIZE, messages.size());
    }

    private LyChatSessionContext ensureSessionContext(Long sessionId) {
        LyChatSessionContext context = chatSessionContextMapper.selectOne(new LambdaQueryWrapper<LyChatSessionContext>()
                .eq(LyChatSessionContext::getSessionId, sessionId)
                .eq(LyChatSessionContext::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (context != null) {
            return context;
        }
        context = new LyChatSessionContext();
        context.setId(idGenerator.nextId());
        context.setSessionId(sessionId);
        context.setSummaryVersion(0);
        context.setIsDeleted(0);
        chatSessionContextMapper.insert(context);
        return context;
    }

    private void persistMessageRef(Long messageId, String refType, String refCode, Long refId, Map<String, Object> meta) {
        LyChatMessageRef ref = new LyChatMessageRef();
        ref.setId(idGenerator.nextId());
        ref.setMessageId(messageId);
        ref.setRefType(refType);
        ref.setRefCode(refCode);
        ref.setRefId(refId);
        ref.setRefMetaJson(writeJson(meta));
        ref.setIsDeleted(0);
        chatMessageRefMapper.insert(ref);
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? new HashMap<>() : payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private LyChatSession requireSession(Long sessionId) {
        LyChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || Integer.valueOf(1).equals(session.getIsDeleted())) {
            throw new BizException(ErrorCode.CHAT_SESSION_NOT_FOUND);
        }
        return session;
    }

    private void ensureConfig() {
        if (isBlank(defaultsProperties.getPromptCode())) {
            throw new BizException(ErrorCode.AI_CONFIG_MISSING, "chat default promptCode is missing");
        }
    }

    private String buildTitle(String content) {
        String clean = content.trim().replaceAll("\\s+", " ");
        return clean.length() <= 20 ? clean : clean.substring(0, 20);
    }

    private String shrink(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        String clean = content.replaceAll("\\s+", " ").trim();
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private ChatSessionVO toSessionVO(LyChatSession session) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        vo.setUserId(session.getUserId());
        vo.setBizRefId(session.getBizRefId());
        vo.setTitle(session.getTitle());
        vo.setStatus(session.getStatus());
        vo.setCreatedAt(session.getCreatedAt());
        return vo;
    }

    private ChatMessageVO toMessageVO(LyChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setTokensIn(message.getTokensIn());
        vo.setTokensOut(message.getTokensOut());
        vo.setModel(message.getModel());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }
}
