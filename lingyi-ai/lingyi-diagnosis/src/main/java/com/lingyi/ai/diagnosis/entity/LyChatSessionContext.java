package com.lingyi.ai.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_chat_session_context")
public class LyChatSessionContext {

    @TableId
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    private String summary;

    @TableField("summary_version")
    private Integer summaryVersion;

    @TableField("last_context_message_id")
    private Long lastContextMessageId;

    @TableField("last_message_id")
    private Long lastMessageId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
