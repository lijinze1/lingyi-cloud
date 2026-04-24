package com.lingyi.service.order.client;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductSkuVO {
    private Long id;
    private Long spuId;
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
