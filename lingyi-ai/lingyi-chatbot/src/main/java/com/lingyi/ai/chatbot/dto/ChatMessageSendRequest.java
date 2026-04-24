package com.lingyi.ai.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageSendRequest {

    @NotNull
    private Long sessionId;

    @NotNull
    private Long userId;

    @NotBlank
    private String content;
}
