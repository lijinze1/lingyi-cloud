package com.lingyi.ai.rag.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class KnowledgeFileVO {

    private Long id;
    private Long kbId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Integer parseStatus;
    private Integer indexStatus;
    private String failureReason;
    private LocalDateTime createdAt;
}
