package com.lingyi.ai.rag.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class KnowledgeBaseVO {

    private Long id;
    private String kbCode;
    private String name;
    private String description;
    private String qdrantCollection;
    private String embeddingModel;
    private Integer embeddingDimension;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private Integer status;
    private LocalDateTime createdAt;
}
