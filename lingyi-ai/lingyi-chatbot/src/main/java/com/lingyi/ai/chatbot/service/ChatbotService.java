package com.lingyi.ai.chatbot.service;

import com.lingyi.ai.chatbot.dto.ChatMessageSendRequest;
import com.lingyi.ai.chatbot.dto.ChatSessionCreateRequest;
import com.lingyi.ai.chatbot.vo.ChatMessageVO;
import com.lingyi.ai.chatbot.vo.ChatReplyVO;
import com.lingyi.ai.chatbot.vo.ChatSessionVO;
import java.util.List;

public interface ChatbotService {

    ChatSessionVO createSession(ChatSessionCreateRequest request);

    List<ChatSessionVO> sessions(Long userId);

    List<ChatMessageVO> messages(Long sessionId);

    ChatReplyVO send(ChatMessageSendRequest request);
}
