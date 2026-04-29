package com.lingyi.service.seckill.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SeckillDetailVO {
    private Long activityId;
    private Long activitySkuId;
    private Long spuId;
    private Long skuId;
    private String name;
    private String title;
    private String image;
    private BigDecimal seckillPrice;
    private BigDecimal originPrice;
    private Integer stockAvailable;
    private Integer limitPerUser;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer activityStatus;
}
