package com.lingyi.service.cart.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CartItemVO {
    private Long id;
    private Long skuId;
    private Long spuId;
    private String title;
    private String attrsJson;
    private String mainImage;
    private BigDecimal price;
    private Integer quantity;
    private Integer checked;
    private Integer stockAvailable;
}
