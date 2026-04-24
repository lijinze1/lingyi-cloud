package com.lingyi.ai.prompt.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ly_prompt")
public class LyPrompt {

    @TableId
    private Long id;

    @TableField("category_id")
    private Long categoryId;

    @TableField("prompt_code")
    private String promptCode;

    private String name;

    @TableField("biz_scene")
    private String bizScene;

    private String description;
    private Integer status;

    @TableField("published_version_id")
    private Long publishedVersionId;

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
