package com.lingyi.ai.prompt.dto;

import com.lingyi.common.ai.AiModelConfig;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromptVersionCreateRequest {

    @NotBlank
    private String content;

    private String variablesJson;

    private AiModelConfig modelConfig;
}
