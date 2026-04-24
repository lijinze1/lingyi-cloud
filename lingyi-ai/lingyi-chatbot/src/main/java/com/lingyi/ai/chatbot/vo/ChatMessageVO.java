package com.lingyi.ai.chatbot.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ChatMessageVO {

    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private Integer tokensIn;
    private Integer tokensOut;
    private String model;
    private LocalDateTime createdAt;
}
