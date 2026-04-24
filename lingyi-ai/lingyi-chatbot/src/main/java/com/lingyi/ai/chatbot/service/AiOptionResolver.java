package com.lingyi.ai.chatbot.service;

import com.lingyi.common.ai.AiModelConfig;

public interface AiOptionResolver {

    AiModelConfig resolve(AiModelConfig versionConfig);
}
