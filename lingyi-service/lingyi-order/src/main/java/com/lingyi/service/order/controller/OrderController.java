package com.lingyi.service.order.controller;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.order.dto.CreateOrderRequest;
import com.lingyi.service.order.dto.PaySuccessRequest;
import com.lingyi.service.order.service.OrderService;
import com.lingyi.service.order.vo.OrderVO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        return Result.success(Map.of("service", "lingyi-order", "status", "UP"));
    }

    @PostMapping("/orders")
    public Result<OrderVO> create(@RequestHeader("X-User-Id") Long userId,
                                  @Valid @RequestBody CreateOrderRequest request) {
        return Result.success(orderService.create(userId, request));
    }

    @GetMapping("/orders")
    public Result<List<OrderVO>> list(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(orderService.list(userId));
    }

    @GetMapping("/orders/{orderNo}")
    public Result<OrderVO> detail(@RequestHeader("X-User-Id") Long userId, @PathVariable String orderNo) {
        return Result.success(orderService.detail(userId, orderNo));
    }

    @PostMapping("/orders/{orderNo}/cancel")
    public Result<Void> cancel(@RequestHeader("X-User-Id") Long userId, @PathVariable String orderNo) {
        orderService.cancel(userId, orderNo);
        return Result.success(null);
    }
}
