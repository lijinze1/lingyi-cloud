package com.lingyi.service.product.dto;

import lombok.Data;

@Data
public class ProductPageQuery {
    private String keyword;
    private Long categoryId;
    private Integer status;
    private long pageNo = 1;
    private long pageSize = 20;
}
