package com.lingyi.ai.chatbot.service.impl;

import com.lingyi.ai.chatbot.service.ProductContextProvider;
import org.springframework.stereotype.Component;

@Component
public class DefaultProductContextProvider implements ProductContextProvider {

    @Override
    public String buildContext(Long bizRefId, Long userId, String question) {
        return "";
    }
}
