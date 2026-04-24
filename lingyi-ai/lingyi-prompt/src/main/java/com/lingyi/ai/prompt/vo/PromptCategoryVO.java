package com.lingyi.ai.prompt.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class PromptCategoryVO {

    private Long id;
    private Long parentId;
    private String categoryCode;
    private String categoryName;
    private Integer sortNo;
    private Integer status;
    private LocalDateTime createdAt;
    private List<PromptCategoryVO> children = new ArrayList<>();
}
