package com.lingyi.service.order.client;

import com.lingyi.common.web.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "lingyi-seckill", contextId = "orderSeckillClient")
public interface SeckillClient {

    @PostMapping("/api/seckill/internal/orders/{orderId}/confirm")
    Result<Void> confirmByOrderId(@PathVariable("orderId") Long orderId);

    @PostMapping("/api/seckill/internal/orders/{orderId}/release")
    Result<Void> releaseByOrderId(@PathVariable("orderId") Long orderId);
}
