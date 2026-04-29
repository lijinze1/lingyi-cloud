package com.lingyi.service.order.service;

import com.lingyi.service.order.dto.CreateOrderRequest;
import com.lingyi.service.order.dto.InternalCreateSeckillOrderRequest;
import com.lingyi.service.order.vo.OrderVO;
import java.util.List;

public interface OrderService {
    OrderVO create(Long userId, CreateOrderRequest request);

    OrderVO createSeckillOrder(InternalCreateSeckillOrderRequest request);

    List<OrderVO> list(Long userId);

    OrderVO detail(Long userId, String orderNo);

    OrderVO getByOrderNo(String orderNo);

    OrderVO getById(Long orderId);

    void cancel(Long userId, String orderNo);

    void refund(Long userId, String orderNo);

    void paySuccess(String orderNo, String paymentNo);
}
