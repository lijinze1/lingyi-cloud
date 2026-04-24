package com.lingyi.service.product.vo;

import lombok.Data;

@Data
public class StockVO {
    private Long skuId;
    private Integer stockTotal;
    private Integer stockAvailable;
    private Integer stockLocked;
}
