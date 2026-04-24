package com.lingyi.ai.prompt.vo;

import com.lingyi.common.ai.AiModelConfig;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PromptVersionVO {

    private Long id;
    private Long promptId;
    private Integer versionNo;
    private String content;
    private String variablesJson;
    private AiModelConfig modelConfig;
    private Integer status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
