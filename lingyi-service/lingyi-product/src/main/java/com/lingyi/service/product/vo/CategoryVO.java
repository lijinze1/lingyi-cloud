package com.lingyi.service.product.vo;

import lombok.Data;

@Data
public class CategoryVO {
    private Long id;
    private Long parentId;
    private String name;
    private Integer sortNo;
    private Integer status;
}
