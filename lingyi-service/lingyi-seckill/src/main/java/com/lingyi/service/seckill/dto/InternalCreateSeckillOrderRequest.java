package com.lingyi.service.seckill.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class InternalCreateSeckillOrderRequest {
    private Long userId;
    private Long activityId;
    private Long activitySkuId;
    private Long seckillRecordId;
    private Long spuId;
    private Long skuId;
    private String skuTitle;
    private String skuAttrsJson;
    private BigDecimal price;
    private Integer quantity;
}