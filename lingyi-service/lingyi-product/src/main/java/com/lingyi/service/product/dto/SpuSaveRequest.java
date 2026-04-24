package com.lingyi.service.product.dto;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SpuSaveRequest {
    private Long id;
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    @NotBlank(message = "商品名称不能为空")
    private String name;
    private String subTitle;
    private String mainImage;
    private String detail;
    private Integer status = 1;
    @Valid
    private List<SkuSaveRequest> skus = new ArrayList<>();
}
