package com.lingyi.service.seckill.controller;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.seckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seckill/internal")
@RequiredArgsConstructor
public class InternalSeckillController {

    private final SeckillService seckillService;

    @PostMapping("/orders/{orderId}/confirm")
    public Result<Void> confirmByOrderId(@PathVariable Long orderId) {
        seckillService.confirmByOrderId(orderId);
        return Result.success(null);
    }

    @PostMapping("/orders/{orderId}/release")
    public Result<Void> releaseByOrderId(@PathVariable Long orderId) {
        seckillService.releaseByOrderId(orderId);
        return Result.success(null);
    }
}
