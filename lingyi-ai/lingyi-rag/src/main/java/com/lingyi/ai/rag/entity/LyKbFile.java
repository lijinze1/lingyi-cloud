package com.lingyi.ai.rag.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_kb_file")
public class LyKbFile {

    @TableId
    private Long id;

    @TableField("kb_id")
    private Long kbId;

    @TableField("file_name")
    private String fileName;

    private String bucket;

    @TableField("object_key")
    private String objectKey;

    @TableField("file_url")
    private String fileUrl;

    @TableField("content_type")
    private String contentType;

    private String etag;
    private String checksum;

    @TableField("file_type")
    private String fileType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("parse_status")
    private Integer parseStatus;

    @TableField("index_status")
    private Integer indexStatus;

    @TableField("failure_reason")
    private String failureReason;

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
