package com.lingyi.ai.chatbot.service;

import com.lingyi.common.ai.PublishedPromptSnapshot;
import java.util.Map;

public interface PromptRuntimeService {

    PublishedPromptSnapshot loadPublishedPrompt(String promptCode);

    String renderContent(PublishedPromptSnapshot snapshot, Map<String, Object> variables);
}
