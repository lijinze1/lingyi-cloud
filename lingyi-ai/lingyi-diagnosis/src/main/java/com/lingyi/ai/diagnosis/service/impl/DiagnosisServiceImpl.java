package com.lingyi.ai.diagnosis.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.ai.diagnosis.config.DiagnosisDefaultsProperties;
import com.lingyi.ai.diagnosis.config.MinioProperties;
import com.lingyi.ai.diagnosis.dto.DiagnosisConsultForm;
import com.lingyi.ai.diagnosis.entity.LyChatAttachment;
import com.lingyi.ai.diagnosis.entity.LyChatMessage;
import com.lingyi.ai.diagnosis.entity.LyChatMessageRef;
import com.lingyi.ai.diagnosis.entity.LyChatSession;
import com.lingyi.ai.diagnosis.entity.LyChatSessionContext;
import com.lingyi.ai.diagnosis.mapper.ChatAttachmentMapper;
import com.lingyi.ai.diagnosis.mapper.ChatMessageMapper;
import com.lingyi.ai.diagnosis.mapper.ChatMessageRefMapper;
import com.lingyi.ai.diagnosis.mapper.ChatSessionContextMapper;
import com.lingyi.ai.diagnosis.mapper.ChatSessionMapper;
import com.lingyi.ai.diagnosis.service.AiOptionResolver;
import com.lingyi.ai.diagnosis.service.DiagnosisService;
import com.lingyi.ai.diagnosis.service.PromptRuntimeService;
import com.lingyi.ai.diagnosis.vo.DiagnosisReplyVO;
import com.lingyi.common.ai.AiModelConfig;
import com.lingyi.common.ai.PublishedPromptSnapshot;
import com.lingyi.common.util.SnowflakeIdGenerator;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DiagnosisServiceImpl implements DiagnosisService {

    private static final String BIZ_TYPE = "diagnosis";
    private static final String DISCLAIMER = "\n\n温馨提示：AI 分析仅供健康咨询参考，不能替代医生面诊、检查和处方。若症状持续、加重，或涉及儿童、孕妇、老人及慢病人群，请及时线下就医。";
    private static final int CONTEXT_WINDOW_SIZE = 10;
    private static final int SUMMARY_TRIGGER_SIZE = 16;

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionContextMapper chatSessionContextMapper;
    private final ChatMessageRefMapper chatMessageRefMapper;
    private final ChatAttachmentMapper chatAttachmentMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final DiagnosisDefaultsProperties defaultsProperties;
    private final MinioProperties minioProperties;
    private final MinioClient minioClient;
    private final ChatModel chatModel;
    private final PromptRuntimeService promptRuntimeService;
    private final AiOptionResolver aiOptionResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DiagnosisServiceImpl(ChatSessionMapper chatSessionMapper,
                                ChatMessageMapper chatMessageMapper,
                                ChatSessionContextMapper chatSessionContextMapper,
                                ChatMessageRefMapper chatMessageRefMapper,
                                ChatAttachmentMapper chatAttachmentMapper,
                                SnowflakeIdGenerator idGenerator,
                                DiagnosisDefaultsProperties defaultsProperties,
                                MinioProperties minioProperties,
                                MinioClient minioClient,
                                ChatModel chatModel,
                                PromptRuntimeService promptRuntimeService,
                                AiOptionResolver aiOptionResolver) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.chatSessionContextMapper = chatSessionContextMapper;
        this.chatMessageRefMapper = chatMessageRefMapper;
        this.chatAttachmentMapper = chatAttachmentMapper;
        this.idGenerator = idGenerator;
        this.defaultsProperties = defaultsProperties;
        this.minioProperties = minioProperties;
        this.minioClient = minioClient;
        this.chatModel = chatModel;
        this.promptRuntimeService = promptRuntimeService;
        this.aiOptionResolver = aiOptionResolver;
    }

    @Override
    @Transactional
    public DiagnosisReplyVO consult(DiagnosisConsultForm form) {
        ensureConfig();
        PublishedPromptSnapshot promptSnapshot = promptRuntimeService.loadPublishedPrompt(defaultsProperties.getPromptCode());
        AiModelConfig modelConfig = aiOptionResolver.resolve(promptSnapshot.getModelConfig());
        LyChatSession session = form.getSessionId() == null ? createSession(form.getUserId(), form.getQuestion()) : requireSession(form.getSessionId());
        LyChatSessionContext contextState = ensureSessionContext(session.getId());

        LyChatMessage userMessage = new LyChatMessage();
        userMessage.setId(idGenerator.nextId());
        userMessage.setSessionId(session.getId());
        userMessage.setRole("user");
        userMessage.setContent(form.getQuestion().trim());
        userMessage.setTokensIn(Math.max(1, form.getQuestion().length() / 4));
        userMessage.setModel(modelConfig.getModel());
        userMessage.setIsDeleted(0);
        chatMessageMapper.insert(userMessage);

        List<UploadedDiagnosisImage> images = uploadImages(session.getId(), userMessage.getId(), form.getImages());
        refreshSummaryIfNeeded(session.getId(), contextState);
        String answer = callVisionModel(session.getId(), form.getQuestion(), images, contextState, promptSnapshot, modelConfig) + DISCLAIMER;

        LyChatMessage assistantMessage = new LyChatMessage();
        assistantMessage.setId(idGenerator.nextId());
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(answer);
        assistantMessage.setTokensOut(Math.max(1, answer.length() / 4));
        assistantMessage.setModel(modelConfig.getModel());
        assistantMessage.setIsDeleted(0);
        chatMessageMapper.insert(assistantMessage);

        persistMessageRef(assistantMessage.getId(), "prompt", promptSnapshot.getPromptCode(), promptSnapshot.getPromptVersionId(),
                Map.of("promptId", promptSnapshot.getPromptId(), "promptVersionId", promptSnapshot.getPromptVersionId()));
        contextState.setLastMessageId(assistantMessage.getId());
        chatSessionContextMapper.updateById(contextState);
        refreshSummaryIfNeeded(session.getId(), contextState);

        DiagnosisReplyVO reply = new DiagnosisReplyVO();
        reply.setSessionId(session.getId());
        reply.setAnswer(answer);
        reply.setImageUrls(images.stream().map(UploadedDiagnosisImage::fileUrl).toList());
        return reply;
    }

    private String callVisionModel(Long sessionId,
                                   String question,
                                   List<UploadedDiagnosisImage> images,
                                   LyChatSessionContext contextState,
                                   PublishedPromptSnapshot promptSnapshot,
                                   AiModelConfig modelConfig) {
        List<Message> messages = new ArrayList<>();
        String systemPrompt = promptRuntimeService.renderContent(promptSnapshot, Map.of(
                "conversationSummary", safeText(contextState.getSummary())));
        messages.add(new SystemMessage(systemPrompt));
        if (contextState.getSummary() != null && !contextState.getSummary().isBlank()) {
            messages.add(new SystemMessage("历史问诊摘要: " + contextState.getSummary()));
        }
        List<LyChatMessage> history = chatMessageMapper.selectList(new LambdaQueryWrapper<LyChatMessage>()
                .eq(LyChatMessage::getSessionId, sessionId)
                .eq(LyChatMessage::getIsDeleted, 0)
                .gt(contextState.getLastContextMessageId() != null, LyChatMessage::getId, contextState.getLastContextMessageId())
                .orderByAsc(LyChatMessage::getCreatedAt));
        for (LyChatMessage historyMessage : trimToWindow(history)) {
            if ("assistant".equals(historyMessage.getRole())) {
                messages.add(new AssistantMessage(historyMessage.getContent()));
            } else {
                messages.add(new UserMessage(historyMessage.getContent()));
            }
        }
        List<Media> mediaList = new ArrayList<>();
        for (UploadedDiagnosisImage image : images) {
            mediaList.add(new Media(MimeTypeUtils.parseMimeType(image.contentType()),
                    new ByteArrayResource(image.bytes())));
        }
        messages.add(UserMessage.builder().text(question).media(mediaList).build());
        Prompt prompt = new Prompt(messages, buildOptions(modelConfig));
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    private List<UploadedDiagnosisImage> uploadImages(Long sessionId, Long messageId, MultipartFile[] images) {
        List<UploadedDiagnosisImage> payloads = new ArrayList<>();
        if (images == null || images.length == 0) {
            return payloads;
        }
        try {
            ensureBucket();
            for (MultipartFile image : images) {
                if (image == null || image.isEmpty()) {
                    continue;
                }
                long attachmentId = idGenerator.nextId();
                String fileName = image.getOriginalFilename() == null ? "image.bin" : image.getOriginalFilename();
                String objectKey = "diagnosis/" + sessionId + "/" + attachmentId + "-" + fileName;
                byte[] bytes = image.getBytes();
                String contentType = image.getContentType() == null ? MimeTypeUtils.IMAGE_JPEG_VALUE : image.getContentType();
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(objectKey)
                        .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                        .contentType(contentType)
                        .build());
                String fileUrl = buildFileUrl(objectKey);

                LyChatAttachment attachment = new LyChatAttachment();
                attachment.setId(attachmentId);
                attachment.setSessionId(sessionId);
                attachment.setMessageId(messageId);
                attachment.setSourceType("image");
                attachment.setBucket(minioProperties.getBucket());
                attachment.setObjectKey(objectKey);
                attachment.setFileUrl(fileUrl);
                attachment.setContentType(contentType);
                attachment.setFileName(fileName);
                attachment.setFileSize(image.getSize());
                attachment.setChecksum(md5(bytes));
                attachment.setIsDeleted(0);
                chatAttachmentMapper.insert(attachment);

                persistMessageRef(messageId, "attachment", "image", attachmentId,
                        Map.of("fileName", fileName, "contentType", contentType));
                payloads.add(new UploadedDiagnosisImage(attachmentId, fileUrl, contentType, bytes));
            }
            return payloads;
        } catch (Exception ex) {
            throw new BizException(ErrorCode.KB_FILE_UPLOAD_FAILED, ex.getMessage());
        }
    }

    private LyChatSession createSession(Long userId, String question) {
        LyChatSession session = new LyChatSession();
        session.setId(idGenerator.nextId());
        session.setUserId(userId);
        session.setBizType(BIZ_TYPE);
        session.setTitle(question.length() <= 20 ? question : question.substring(0, 20));
        session.setStatus(1);
        session.setIsDeleted(0);
        chatSessionMapper.insert(session);
        ensureSessionContext(session.getId());
        return session;
    }

    private LyChatSession requireSession(Long sessionId) {
        LyChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || Integer.valueOf(1).equals(session.getIsDeleted())) {
            throw new BizException(ErrorCode.CHAT_SESSION_NOT_FOUND);
        }
        return session;
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
        contextState.setSummary(summarizeMessages(contextState.getSummary(), toSummarize));
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
        return shrink(String.join(" | ", lines), 2000);
    }

    private List<LyChatMessage> trimToWindow(List<LyChatMessage> messages) {
        if (messages.size() <= CONTEXT_WINDOW_SIZE) {
            return messages;
        }
        return messages.subList(messages.size() - CONTEXT_WINDOW_SIZE, messages.size());
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
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private void ensureConfig() {
        if (isBlank(defaultsProperties.getPromptCode())) {
            throw new BizException(ErrorCode.AI_CONFIG_MISSING, "diagnosis default promptCode is missing");
        }
        if (isBlank(minioProperties.getEndpoint())
                || isBlank(minioProperties.getAccessKey())
                || isBlank(minioProperties.getSecretKey())
                || isBlank(minioProperties.getBucket())) {
            throw new BizException(ErrorCode.AI_CONFIG_MISSING, "minio config is incomplete");
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
        }
    }

    private String buildFileUrl(String objectKey) {
        if (!isBlank(minioProperties.getPublicBaseUrl())) {
            return minioProperties.getPublicBaseUrl().replaceAll("/$", "") + "/" + objectKey;
        }
        return minioProperties.getEndpoint().replaceAll("/$", "") + "/" + minioProperties.getBucket() + "/" + objectKey;
    }

    private String md5(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] md5 = digest.digest(bytes);
        StringBuilder builder = new StringBuilder();
        for (byte b : md5) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private DashScopeChatOptions buildOptions(AiModelConfig modelConfig) {
        var builder = DashScopeChatOptions.builder();
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

    private record UploadedDiagnosisImage(Long attachmentId, String fileUrl, String contentType, byte[] bytes) {
    }
}
