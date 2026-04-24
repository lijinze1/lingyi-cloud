package com.lingyi.ai.diagnosis.service;

import com.lingyi.common.ai.AiModelConfig;

public interface AiOptionResolver {

    AiModelConfig resolve(AiModelConfig versionConfig);
}
