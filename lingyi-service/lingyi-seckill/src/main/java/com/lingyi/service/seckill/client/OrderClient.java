package com.lingyi.service.seckill.client;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.seckill.dto.InternalCreateSeckillOrderRequest;
import com.lingyi.service.seckill.vo.LinkedOrderVO;
import com.lingyi.service.seckill.vo.SeckillOrderCreateVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "lingyi-order", contextId = "seckillOrderClient")
public interface OrderClient {
    @PostMapping("/api/order/internal/seckill-orders")
    Result<SeckillOrderCreateVO> createSeckillOrder(@RequestBody InternalCreateSeckillOrderRequest request);

    @GetMapping("/api/order/internal/orders/id/{orderId}")
    Result<LinkedOrderVO> getOrderById(@PathVariable("orderId") Long orderId);
}
