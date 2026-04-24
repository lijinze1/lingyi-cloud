package com.lingyi.ai.chatbot.service.impl;

import com.lingyi.ai.chatbot.config.ChatDefaultsProperties;
import com.lingyi.ai.chatbot.service.AiOptionResolver;
import com.lingyi.common.ai.AiModelConfig;
import org.springframework.stereotype.Component;

@Component
public class ChatAiOptionResolver implements AiOptionResolver {

    private final ChatDefaultsProperties defaultsProperties;

    public ChatAiOptionResolver(ChatDefaultsProperties defaultsProperties) {
        this.defaultsProperties = defaultsProperties;
    }

    @Override
    public AiModelConfig resolve(AiModelConfig versionConfig) {
        AiModelConfig resolved = new AiModelConfig();
        resolved.setModel(preferred(versionConfig == null ? null : versionConfig.getModel(), defaultsProperties.getModel()));
        resolved.setTemperature(preferred(versionConfig == null ? null : versionConfig.getTemperature(), defaultsProperties.getTemperature()));
        resolved.setTopP(preferred(versionConfig == null ? null : versionConfig.getTopP(), defaultsProperties.getTopP()));
        resolved.setTopK(preferred(versionConfig == null ? null : versionConfig.getTopK(), defaultsProperties.getTopK()));
        resolved.setMaxTokens(preferred(versionConfig == null ? null : versionConfig.getMaxTokens(), defaultsProperties.getMaxTokens()));
        resolved.setPresencePenalty(preferred(versionConfig == null ? null : versionConfig.getPresencePenalty(), defaultsProperties.getPresencePenalty()));
        resolved.setFrequencyPenalty(preferred(versionConfig == null ? null : versionConfig.getFrequencyPenalty(), defaultsProperties.getFrequencyPenalty()));
        return resolved;
    }

    private <T> T preferred(T preferred, T fallback) {
        return preferred != null ? preferred : fallback;
    }
}
