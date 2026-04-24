package com.lingyi.service.product.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkuSaveRequest {
    private Long id;
    private String skuCode;
    @NotBlank(message = "SKU 标题不能为空")
    private String title;
    private String attrsJson;
    @NotNull(message = "销售价不能为空")
    @DecimalMin(value = "0.01", message = "销售价必须大于 0")
    private BigDecimal price;
    private BigDecimal originPrice;
    private Integer status = 1;
    private Integer stockTotal = 0;
}
