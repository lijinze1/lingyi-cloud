package com.lingyi.service.seckill.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SeckillSkuVO {
    private Long activitySkuId;
    private Long activityId;
    private Long spuId;
    private Long skuId;
    private String spuName;
    private String skuTitle;
    private String mainImage;
    private BigDecimal seckillPrice;
    private BigDecimal originPrice;
    private Integer stockAvailable;
    private Integer limitPerUser;
}