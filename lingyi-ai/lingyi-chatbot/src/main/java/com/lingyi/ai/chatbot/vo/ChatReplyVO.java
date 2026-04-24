package com.lingyi.ai.chatbot.vo;

import lombok.Data;

@Data
public class ChatReplyVO {

    private ChatSessionVO session;
    private ChatMessageVO assistantMessage;
}
