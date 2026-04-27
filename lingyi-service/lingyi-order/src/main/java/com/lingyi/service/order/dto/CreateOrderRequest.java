package com.lingyi.service.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @Valid
    @NotEmpty(message = "订单商品不能为空")
    private List<CreateOrderItemRequest> items = new ArrayList<>();
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
}
