package com.lingyi.ai.chatbot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ly_prompt_version")
public class LyPromptVersion {

    @TableId
    private Long id;

    @TableField("prompt_id")
    private Long promptId;

    private String content;

    @TableField("variables_json")
    private String variablesJson;

    @TableField("model_config_json")
    private String modelConfigJson;

    private Integer status;

    @TableField("is_deleted")
    private Integer isDeleted;
}
