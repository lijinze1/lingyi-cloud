package com.lingyi.service.order.controller;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.order.dto.PaySuccessRequest;
import com.lingyi.service.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order/internal")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @PostMapping("/orders/{orderNo}/pay-success")
    public Result<Void> paySuccess(@PathVariable String orderNo, @Valid @RequestBody PaySuccessRequest request) {
        orderService.paySuccess(orderNo, request.getPaymentNo());
        return Result.success(null);
    }
}
