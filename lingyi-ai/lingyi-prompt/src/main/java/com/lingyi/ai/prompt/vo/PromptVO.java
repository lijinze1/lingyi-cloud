package com.lingyi.ai.prompt.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PromptVO {

    private Long id;
    private Long categoryId;
    private String promptCode;
    private String name;
    private String bizScene;
    private String description;
    private Integer status;
    private Long publishedVersionId;
    private LocalDateTime createdAt;
}
