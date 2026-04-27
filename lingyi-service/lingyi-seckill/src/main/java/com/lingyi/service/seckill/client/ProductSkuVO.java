package com.lingyi.service.seckill.client;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductSkuVO {
    private Long id;
    private Long spuId;
    private String spuName;
    private String mainImage;
    private String skuCode;
    private String title;
    private String attrsJson;
    private BigDecimal price;
    private BigDecimal originPrice;
    private Integer stockAvailable;
}