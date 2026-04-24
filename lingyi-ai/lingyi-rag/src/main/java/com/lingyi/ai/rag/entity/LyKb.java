package com.lingyi.ai.rag.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_kb")
public class LyKb {

    @TableId
    private Long id;

    @TableField("kb_code")
    private String kbCode;

    private String name;
    private String description;

    @TableField("qdrant_collection")
    private String qdrantCollection;

    @TableField("embedding_model")
    private String embeddingModel;

    @TableField("embedding_dimension")
    private Integer embeddingDimension;

    @TableField("chunk_size")
    private Integer chunkSize;

    @TableField("chunk_overlap")
    private Integer chunkOverlap;

    private Integer status;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
