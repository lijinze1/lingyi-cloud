package com.lingyi.ai.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PromptCategorySaveRequest {

    @NotNull
    private Long parentId = 0L;

    @NotBlank
    @Size(max = 64)
    private String categoryCode;

    @NotBlank
    @Size(max = 128)
    private String categoryName;

    private Integer sortNo = 0;

    private Integer status = 1;
}
