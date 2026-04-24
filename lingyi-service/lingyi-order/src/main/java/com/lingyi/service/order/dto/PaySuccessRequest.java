package com.lingyi.service.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaySuccessRequest {
    @NotBlank(message = "支付单号不能为空")
    private String paymentNo;
}
