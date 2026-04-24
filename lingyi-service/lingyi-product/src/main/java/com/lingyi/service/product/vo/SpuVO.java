package com.lingyi.service.product.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SpuVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subTitle;
    private String mainImage;
    private Integer status;
    private BigDecimal minPrice;
    private Integer stockAvailable;
}
