package com.lingyi.service.product.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ProductDetailVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subTitle;
    private String mainImage;
    private String detail;
    private Integer status;
    private List<SkuVO> skus = new ArrayList<>();
}
