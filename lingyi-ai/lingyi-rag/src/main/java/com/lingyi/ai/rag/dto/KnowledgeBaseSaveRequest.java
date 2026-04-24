package com.lingyi.ai.rag.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeBaseSaveRequest {

    @NotBlank
    @Size(max = 64)
    private String kbCode;

    @NotBlank
    @Size(max = 128)
    private String name;

    @Size(max = 255)
    private String description;

    @NotBlank
    @Size(max = 128)
    private String qdrantCollection;

    @NotBlank
    @Size(max = 128)
    private String embeddingModel;

    private Integer embeddingDimension;

    @NotNull
    @Min(100)
    private Integer chunkSize = 500;

    @NotNull
    @Min(0)
    private Integer chunkOverlap = 50;

    private Integer status = 1;
}
