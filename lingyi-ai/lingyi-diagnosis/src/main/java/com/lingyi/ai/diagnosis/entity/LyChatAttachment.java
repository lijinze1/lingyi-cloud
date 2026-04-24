package com.lingyi.ai.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_chat_attachment")
public class LyChatAttachment {

    @TableId
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("message_id")
    private Long messageId;

    @TableField("source_type")
    private String sourceType;

    private String bucket;

    @TableField("object_key")
    private String objectKey;

    @TableField("file_url")
    private String fileUrl;

    @TableField("content_type")
    private String contentType;

    @TableField("file_name")
    private String fileName;

    @TableField("file_size")
    private Long fileSize;

    private String checksum;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
