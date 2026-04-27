package com.lingyi.service.order.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InternalCreateSeckillOrderRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long activityId;
    @NotNull
    private Long activitySkuId;
    @NotNull
    private Long seckillRecordId;
    @NotNull
    private Long spuId;
    @NotNull
    private Long skuId;
    @NotBlank
    private String skuTitle;
    private String skuAttrsJson;
    @NotNull
    private BigDecimal price;
    @NotNull
    private Integer quantity;
}