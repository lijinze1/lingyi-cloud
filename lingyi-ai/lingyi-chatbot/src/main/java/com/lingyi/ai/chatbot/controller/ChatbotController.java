package com.lingyi.ai.chatbot.controller;

import com.lingyi.ai.chatbot.dto.ChatMessageSendRequest;
import com.lingyi.ai.chatbot.dto.ChatSessionCreateRequest;
import com.lingyi.ai.chatbot.service.ChatbotService;
import com.lingyi.ai.chatbot.vo.ChatMessageVO;
import com.lingyi.ai.chatbot.vo.ChatReplyVO;
import com.lingyi.ai.chatbot.vo.ChatSessionVO;
import com.lingyi.common.web.domain.Result;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/chat")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/sessions")
    public Result<ChatSessionVO> createSession(@Valid @RequestBody ChatSessionCreateRequest request) {
        return Result.success(chatbotService.createSession(request));
    }

    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> sessions(@RequestParam(required = false) Long userId) {
        return Result.success(chatbotService.sessions(userId));
    }

    @GetMapping("/messages")
    public Result<List<ChatMessageVO>> messages(@RequestParam Long sessionId) {
        return Result.success(chatbotService.messages(sessionId));
    }

    @PostMapping("/messages")
    public Result<ChatReplyVO> send(@Valid @RequestBody ChatMessageSendRequest request) {
        return Result.success(chatbotService.send(request));
    }
}
