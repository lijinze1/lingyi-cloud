package com.lingyi.service.order.controller;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.order.dto.InternalCreateSeckillOrderRequest;
import com.lingyi.service.order.dto.PaySuccessRequest;
import com.lingyi.service.order.service.OrderService;
import com.lingyi.service.order.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/seckill-orders")
    public Result<OrderVO> createSeckillOrder(@Valid @RequestBody InternalCreateSeckillOrderRequest request) {
        return Result.success(orderService.createSeckillOrder(request));
    }

    @GetMapping("/orders/{orderNo}")
    public Result<OrderVO> getByOrderNo(@PathVariable String orderNo) {
        return Result.success(orderService.getByOrderNo(orderNo));
    }

    @PostMapping("/orders/{orderNo}/pay-success")
    public Result<Void> paySuccess(@PathVariable String orderNo, @Valid @RequestBody PaySuccessRequest request) {
        orderService.paySuccess(orderNo, request.getPaymentNo());
        return Result.success(null);
    }
}