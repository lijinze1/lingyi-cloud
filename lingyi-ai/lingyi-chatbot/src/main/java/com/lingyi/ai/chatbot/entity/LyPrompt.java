package com.lingyi.ai.chatbot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ly_prompt")
public class LyPrompt {

    @TableId
    private Long id;

    @TableField("prompt_code")
    private String promptCode;

    private String name;

    @TableField("published_version_id")
    private Long publishedVersionId;

    private Integer status;

    @TableField("is_deleted")
    private Integer isDeleted;
}
