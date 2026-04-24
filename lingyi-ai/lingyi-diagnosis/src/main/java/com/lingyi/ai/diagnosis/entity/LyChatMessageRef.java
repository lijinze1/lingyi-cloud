package com.lingyi.ai.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_chat_message_ref")
public class LyChatMessageRef {

    @TableId
    private Long id;

    @TableField("message_id")
    private Long messageId;

    @TableField("ref_type")
    private String refType;

    @TableField("ref_code")
    private String refCode;

    @TableField("ref_id")
    private Long refId;

    @TableField("ref_meta_json")
    private String refMetaJson;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
