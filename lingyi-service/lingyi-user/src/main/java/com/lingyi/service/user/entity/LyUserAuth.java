package com.lingyi.service.user.entity;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("ly_user_auth")
@Data
public class LyUserAuth {

    @TableId
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("auth_type")
    private String authType;

    @TableField("auth_key")
    private String authKey;

    @TableField("auth_secret")
    private String authSecret;

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

