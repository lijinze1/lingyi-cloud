package com.lingyi.service.payment.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderSnapshotVO {
    private String orderNo;
    private Long userId;
    private BigDecimal payAmount;
    private Integer status;
    private Integer payStatus;
}