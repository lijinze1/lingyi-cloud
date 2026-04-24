package com.lingyi.service.payment.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PaymentVO {
    private String paymentNo;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private Integer payChannel;
    private Integer payStatus;
    private LocalDateTime paidAt;
}
