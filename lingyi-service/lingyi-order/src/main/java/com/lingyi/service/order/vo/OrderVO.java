package com.lingyi.service.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Integer orderType;
    private Integer status;
    private Integer payStatus;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items = new ArrayList<>();
}