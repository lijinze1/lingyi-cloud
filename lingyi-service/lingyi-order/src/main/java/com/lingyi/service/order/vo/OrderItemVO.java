package com.lingyi.service.order.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderItemVO {
    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private String skuAttrsJson;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal amount;
}
