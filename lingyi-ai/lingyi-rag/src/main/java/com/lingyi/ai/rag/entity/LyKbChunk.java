package com.lingyi.ai.rag.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_kb_chunk")
public class LyKbChunk {

    @TableId
    private Long id;

    @TableField("kb_id")
    private Long kbId;

    @TableField("file_id")
    private Long fileId;

    @TableField("chunk_index")
    private Integer chunkIndex;

    private String content;

    @TableField("vector_id")
    private String vectorId;

    @TableField("token_count")
    private Integer tokenCount;

    @TableField("metadata_json")
    private String metadataJson;

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
