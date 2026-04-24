package com.lingyi.service.order.client;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockChangeRequest {
    @NotNull
    private Long skuId;
    @NotNull
    @Min(1)
    private Integer quantity;
}
