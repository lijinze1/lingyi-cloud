package com.lingyi.service.cart.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CartUpdateRequest {
    @Min(value = 1, message = "数量必须大于 0")
    private Integer quantity;
    private Integer checked;
}
