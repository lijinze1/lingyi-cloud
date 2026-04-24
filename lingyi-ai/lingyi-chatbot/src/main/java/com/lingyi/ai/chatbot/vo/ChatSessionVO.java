package com.lingyi.ai.chatbot.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ChatSessionVO {

    private Long id;
    private Long userId;
    private Long bizRefId;
    private String title;
    private Integer status;
    private LocalDateTime createdAt;
}
