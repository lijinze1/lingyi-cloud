package com.lingyi.service.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategorySaveRequest {
    private Long id;
    private Long parentId = 0L;
    @NotBlank(message = "分类名称不能为空")
    private String name;
    private Integer sortNo = 0;
    @NotNull(message = "分类状态不能为空")
    private Integer status = 1;
}
