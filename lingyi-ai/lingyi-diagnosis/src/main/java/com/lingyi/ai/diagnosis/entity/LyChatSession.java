package com.lingyi.ai.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_chat_session")
public class LyChatSession {

    @TableId
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_ref_id")
    private Long bizRefId;

    private String title;
    private Integer status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
