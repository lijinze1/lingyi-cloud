package com.lingyi.ai.chatbot.service;

public interface ProductContextProvider {

    String buildContext(Long bizRefId, Long userId, String question);
}
