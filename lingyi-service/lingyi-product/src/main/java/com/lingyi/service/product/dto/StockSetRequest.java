package com.lingyi.service.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockSetRequest {
    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;
    @NotNull(message = "总库存不能为空")
    @Min(value = 0, message = "总库存不能小于 0")
    private Integer stockTotal;
    @NotNull(message = "可用库存不能为空")
    @Min(value = 0, message = "可用库存不能小于 0")
    private Integer stockAvailable;
}
