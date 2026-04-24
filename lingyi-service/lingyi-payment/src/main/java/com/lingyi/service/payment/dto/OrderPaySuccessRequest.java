package com.lingyi.service.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderPaySuccessRequest {
    @NotBlank(message = "支付单号不能为空")
    private String paymentNo;
}
