package com.lingyi.ai.prompt.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_prompt_version")
public class LyPromptVersion {

    @TableId
    private Long id;

    @TableField("prompt_id")
    private Long promptId;

    @TableField("version_no")
    private Integer versionNo;

    private String content;

    @TableField("variables_json")
    private String variablesJson;

    @TableField("model_config_json")
    private String modelConfigJson;

    private Integer status;

    @TableField("published_at")
    private LocalDateTime publishedAt;

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
