package com.lingyi.ai.chatbot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatSessionCreateRequest {

    @NotNull
    private Long userId;

    private Long bizRefId;

    @Size(max = 128)
    private String title;
}
