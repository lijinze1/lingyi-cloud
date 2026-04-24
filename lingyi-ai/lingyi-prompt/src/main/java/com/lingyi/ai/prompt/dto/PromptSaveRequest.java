package com.lingyi.ai.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PromptSaveRequest {

    @NotNull
    private Long categoryId;

    @NotBlank
    @Size(max = 64)
    private String promptCode;

    @NotBlank
    @Size(max = 128)
    private String name;

    @NotBlank
    @Size(max = 64)
    private String bizScene;

    @Size(max = 255)
    private String description;

    private Integer status = 1;
}
