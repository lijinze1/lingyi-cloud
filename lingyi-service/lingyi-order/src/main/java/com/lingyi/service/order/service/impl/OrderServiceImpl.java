package com.lingyi.service.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.order.client.ProductClient;
import com.lingyi.service.order.client.ProductSkuVO;
import com.lingyi.service.order.client.StockChangeRequest;
import com.lingyi.service.order.dto.CreateOrderItemRequest;
import com.lingyi.service.order.dto.CreateOrderRequest;
import com.lingyi.service.order.entity.LyOrder;
import com.lingyi.service.order.entity.LyOrderItem;
import com.lingyi.service.order.entity.LyOrderLog;
import com.lingyi.service.order.mapper.OrderItemMapper;
import com.lingyi.service.order.mapper.OrderLogMapper;
import com.lingyi.service.order.mapper.OrderMapper;
import com.lingyi.service.order.service.OrderService;
import com.lingyi.service.order.vo.OrderItemVO;
import com.lingyi.service.order.vo.OrderVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int STATUS_WAIT_PAY = 10;
    private static final int STATUS_PAID = 20;
    private static final int STATUS_CANCELLED = 50;
    private static final int PAY_UNPAID = 0;
    private static final int PAY_PAID = 1;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderLogMapper orderLogMapper;
    private final ProductClient productClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(Long userId, CreateOrderRequest request) {
        String orderNo = nextOrderNo();
        List<LyOrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<StockChangeRequest> locked = new ArrayList<>();
        try {
            for (CreateOrderItemRequest itemRequest : request.getItems()) {
                ProductSkuVO sku = requireSku(itemRequest.getSkuId());
                StockChangeRequest stockRequest = new StockChangeRequest();
                stockRequest.setSkuId(itemRequest.getSkuId());
                stockRequest.setQuantity(itemRequest.getQuantity());
                productClient.lockStock(stockRequest);
                locked.add(stockRequest);

                BigDecimal amount = sku.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                totalAmount = totalAmount.add(amount);
                LyOrderItem item = new LyOrderItem();
                item.setOrderNo(orderNo);
                item.setSpuId(sku.getSpuId());
                item.setSkuId(sku.getId());
                item.setSkuTitle(sku.getTitle());
                item.setSkuAttrsJson(sku.getAttrsJson());
                item.setPrice(sku.getPrice());
                item.setQuantity(itemRequest.getQuantity());
                item.setAmount(amount);
                items.add(item);
            }
        } catch (RuntimeException ex) {
            locked.forEach(stock -> safeRelease(stock.getSkuId(), stock.getQuantity()));
            throw ex;
        }

        LyOrder order = new LyOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setOrderType(0);
        order.setStatus(STATUS_WAIT_PAY);
        order.setPayStatus(PAY_UNPAID);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setRemark(request.getRemark());
        orderMapper.insert(order);
        for (LyOrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        addLog(order, userId, "CREATE", "创建订单并锁定库存");
        return toVO(order);
    }

    @Override
    public List<OrderVO> list(Long userId) {
        return orderMapper.selectList(new LambdaQueryWrapper<LyOrder>()
                        .eq(LyOrder::getUserId, userId)
                        .orderByDesc(LyOrder::getCreatedAt))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public OrderVO detail(Long userId, String orderNo) {
        LyOrder order = requireOrder(userId, orderNo);
        return toVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, String orderNo) {
        LyOrder order = requireOrder(userId, orderNo);
        if (!Objects.equals(order.getStatus(), STATUS_WAIT_PAY)) {
            throw new BizException("O0409", "只有待支付订单可以取消");
        }
        List<LyOrderItem> items = orderItems(order.getId());
        for (LyOrderItem item : items) {
            safeRelease(item.getSkuId(), item.getQuantity());
        }
        order.setStatus(STATUS_CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        orderMapper.updateById(order);
        addLog(order, userId, "CANCEL", "取消订单并释放库存");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String orderNo, String paymentNo) {
        LyOrder order = orderMapper.selectOne(new LambdaQueryWrapper<LyOrder>().eq(LyOrder::getOrderNo, orderNo).last("LIMIT 1"));
        if (order == null) {
            throw new BizException("O0404", "订单不存在");
        }
        if (Objects.equals(order.getPayStatus(), PAY_PAID)) {
            return;
        }
        if (!Objects.equals(order.getStatus(), STATUS_WAIT_PAY)) {
            throw new BizException("O0410", "订单状态不允许支付");
        }
        List<LyOrderItem> items = orderItems(order.getId());
        for (LyOrderItem item : items) {
            StockChangeRequest stockRequest = new StockChangeRequest();
            stockRequest.setSkuId(item.getSkuId());
            stockRequest.setQuantity(item.getQuantity());
            productClient.deductStock(stockRequest);
        }
        order.setStatus(STATUS_PAID);
        order.setPayStatus(PAY_PAID);
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);
        addLog(order, order.getUserId(), "PAY_SUCCESS", "支付成功，支付单号：" + paymentNo);
    }

    private ProductSkuVO requireSku(Long skuId) {
        ProductSkuVO sku = productClient.getSku(skuId).getData();
        if (sku == null) {
            throw new BizException("O0404", "商品不存在或已下架");
        }
        return sku;
    }

    private LyOrder requireOrder(Long userId, String orderNo) {
        LyOrder order = orderMapper.selectOne(new LambdaQueryWrapper<LyOrder>()
                .eq(LyOrder::getUserId, userId)
                .eq(LyOrder::getOrderNo, orderNo)
                .last("LIMIT 1"));
        if (order == null) {
            throw new BizException("O0404", "订单不存在");
        }
        return order;
    }

    private List<LyOrderItem> orderItems(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<LyOrderItem>().eq(LyOrderItem::getOrderId, orderId));
    }

    private OrderVO toVO(LyOrder order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setStatus(order.getStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setPaidAt(order.getPaidAt());
        vo.setCancelledAt(order.getCancelledAt());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setItems(orderItems(order.getId()).stream().map(this::toItemVO).toList());
        return vo;
    }

    private OrderItemVO toItemVO(LyOrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setSkuId(item.getSkuId());
        vo.setSpuId(item.getSpuId());
        vo.setSkuTitle(item.getSkuTitle());
        vo.setSkuAttrsJson(item.getSkuAttrsJson());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setAmount(item.getAmount());
        return vo;
    }

    private void addLog(LyOrder order, Long operatorId, String action, String content) {
        LyOrderLog log = new LyOrderLog();
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setAction(action);
        log.setOperatorId(operatorId);
        log.setOperatorName("system");
        log.setContent(content);
        orderLogMapper.insert(log);
    }

    private void safeRelease(Long skuId, Integer quantity) {
        StockChangeRequest request = new StockChangeRequest();
        request.setSkuId(skuId);
        request.setQuantity(quantity);
        try {
            productClient.releaseStock(request);
        } catch (RuntimeException ignored) {
            // 释放库存失败不覆盖原始业务异常，后续可接入补偿任务。
        }
    }

    private String nextOrderNo() {
        return "LY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + ThreadLocalRandom.current().nextInt(100, 999);
    }
}
