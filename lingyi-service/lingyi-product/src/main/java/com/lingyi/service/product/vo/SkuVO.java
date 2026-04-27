package com.lingyi.service.product.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SkuVO {
    private Long id;
    private Long spuId;
    private String spuName;
    private String mainImage;
    private String skuCode;
    private String title;
    private String attrsJson;
    private BigDecimal price;
    private BigDecimal originPrice;
    private Integer status;
    private Integer stockTotal;
    private Integer stockAvailable;
    private Integer stockLocked;
}
