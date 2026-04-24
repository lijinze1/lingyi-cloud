package com.lingyi.ai.rag.vo;

import lombok.Data;

@Data
public class KnowledgeChunkVO {

    private Long id;
    private Long kbId;
    private Long fileId;
    private Integer chunkIndex;
    private String content;
    private String vectorId;
    private Integer tokenCount;
    private String metadataJson;
    private Integer pageNo;
    private String sourceType;
}
