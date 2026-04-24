package com.lingyi.ai.diagnosis.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_chat_message")
public class LyChatMessage {

    @TableId
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    private String role;
    private String content;

    @TableField("tokens_in")
    private Integer tokensIn;

    @TableField("tokens_out")
    private Integer tokensOut;

    private String model;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
