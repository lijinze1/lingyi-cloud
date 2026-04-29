package com.lingyi.service.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.domain.Result;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.order.client.ProductClient;
import com.lingyi.service.order.client.ProductSkuVO;
import com.lingyi.service.order.client.SeckillClient;
import com.lingyi.service.order.client.StockChangeRequest;
import com.lingyi.service.order.dto.CreateOrderItemRequest;
import com.lingyi.service.order.dto.CreateOrderRequest;
import com.lingyi.service.order.dto.InternalCreateSeckillOrderRequest;
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

    private static final String SUCCESS_CODE = ErrorCode.SUCCESS.getCode();
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_SECKILL = 1;
    private static final int STATUS_WAIT_PAY = 10;
    private static final int STATUS_PAID = 20;
    private static final int STATUS_CANCELLED = 50;
    private static final int STATUS_REFUNDED = 60;
    private static final int PAY_UNPAID = 0;
    private static final int PAY_PAID = 1;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderLogMapper orderLogMapper;
    private final ProductClient productClient;
    private final SeckillClient seckillClient;

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
                lockStock(stockRequest);
                locked.add(stockRequest);

                BigDecimal amount = sku.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                totalAmount = totalAmount.add(amount);
                items.add(buildOrderItem(orderNo, sku, itemRequest.getQuantity(), amount));
            }
        } catch (RuntimeException ex) {
            locked.forEach(stock -> safeReleaseNormalStock(stock.getSkuId(), stock.getQuantity()));
            throw ex;
        }

        LyOrder order = buildOrder(
                userId,
                TYPE_NORMAL,
                orderNo,
                totalAmount,
                request.getReceiverName(),
                request.getReceiverPhone(),
                request.getReceiverAddress(),
                request.getRemark()
        );
        orderMapper.insert(order);
        saveItems(order, items);
        addLog(order, userId, "CREATE", "创建普通订单并锁定库存");
        return toVO(order);
    }

    @Override
    public OrderVO getById(Long orderId) {
        LyOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("O0404", "订单不存在");
        }
        return toVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createSeckillOrder(InternalCreateSeckillOrderRequest request) {
        String seckillRemark = buildSeckillRemark(request);
        LyOrder existing = orderMapper.selectOne(new LambdaQueryWrapper<LyOrder>()
                .eq(LyOrder::getUserId, request.getUserId())
                .eq(LyOrder::getOrderType, TYPE_SECKILL)
                .eq(LyOrder::getRemark, seckillRemark)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getStatus(), STATUS_CANCELLED)) {
            return toVO(existing);
        }

        String orderNo = nextOrderNo();
        BigDecimal totalAmount = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        LyOrder order = buildOrder(request.getUserId(), TYPE_SECKILL, orderNo, totalAmount, "", "", "", seckillRemark);
        orderMapper.insert(order);

        LyOrderItem item = new LyOrderItem();
        item.setOrderId(order.getId());
        item.setOrderNo(orderNo);
        item.setSpuId(request.getSpuId());
        item.setSkuId(request.getSkuId());
        item.setSkuTitle(request.getSkuTitle());
        item.setSkuAttrsJson(request.getSkuAttrsJson());
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());
        item.setAmount(totalAmount);
        orderItemMapper.insert(item);

        addLog(order, request.getUserId(), "CREATE_SECKILL", "创建秒杀订单");
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
        return toVO(requireOrder(userId, orderNo));
    }

    @Override
    public OrderVO getByOrderNo(String orderNo) {
        LyOrder order = orderMapper.selectOne(new LambdaQueryWrapper<LyOrder>()
                .eq(LyOrder::getOrderNo, orderNo)
                .last("LIMIT 1"));
        if (order == null) {
            throw new BizException("O0404", "订单不存在");
        }
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
        if (Objects.equals(order.getOrderType(), TYPE_SECKILL)) {
            releaseSeckillOrder(order.getId());
        } else {
            for (LyOrderItem item : items) {
                safeReleaseNormalStock(item.getSkuId(), item.getQuantity());
            }
        }
        order.setStatus(STATUS_CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        orderMapper.updateById(order);
        addLog(order, userId, "CANCEL", "取消订单");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long userId, String orderNo) {
        LyOrder order = requireOrder(userId, orderNo);
        if (!Objects.equals(order.getPayStatus(), PAY_PAID) || !Objects.equals(order.getStatus(), STATUS_PAID)) {
            throw new BizException("O0411", "当前订单暂不支持退款");
        }
        order.setStatus(STATUS_REFUNDED);
        orderMapper.updateById(order);
        addLog(order, userId, "REFUND", "用户发起退款并完成退款");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String orderNo, String paymentNo) {
        LyOrder order = orderMapper.selectOne(new LambdaQueryWrapper<LyOrder>()
                .eq(LyOrder::getOrderNo, orderNo)
                .last("LIMIT 1"));
        if (order == null) {
            throw new BizException("O0404", "订单不存在");
        }
        if (Objects.equals(order.getPayStatus(), PAY_PAID)) {
            return;
        }
        if (!Objects.equals(order.getStatus(), STATUS_WAIT_PAY)) {
            throw new BizException("O0410", "订单当前状态不允许支付");
        }
        if (Objects.equals(order.getOrderType(), TYPE_SECKILL)) {
            confirmSeckillOrder(order.getId());
        } else {
            List<LyOrderItem> items = orderItems(order.getId());
            for (LyOrderItem item : items) {
                StockChangeRequest stockRequest = new StockChangeRequest();
                stockRequest.setSkuId(item.getSkuId());
                stockRequest.setQuantity(item.getQuantity());
                deductStock(stockRequest);
            }
        }
        order.setStatus(STATUS_PAID);
        order.setPayStatus(PAY_PAID);
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);
        addLog(order, order.getUserId(), "PAY_SUCCESS", "支付成功，支付单号：" + paymentNo);
    }

    private ProductSkuVO requireSku(Long skuId) {
        Result<ProductSkuVO> result;
        try {
            result = productClient.getSku(skuId);
        } catch (RuntimeException ex) {
            throw new BizException("O0500", "商品服务暂时不可用，请稍后重试");
        }
        if (result == null || !SUCCESS_CODE.equals(result.getCode())) {
            throw new BizException("O0500", resolveMessage(result, "商品服务暂时不可用，请稍后重试"));
        }
        ProductSkuVO sku = result.getData();
        if (sku == null) {
            throw new BizException("O0404", "商品不存在或已下架");
        }
        return sku;
    }

    private LyOrder requireOrder(Long userId, String orderNo) {
        LambdaQueryWrapper<LyOrder> wrapper = new LambdaQueryWrapper<LyOrder>()
                .eq(LyOrder::getOrderNo, orderNo)
                .last("LIMIT 1");
        if (userId != null) {
            wrapper.eq(LyOrder::getUserId, userId);
        }
        LyOrder order = orderMapper.selectOne(wrapper);
        if (order == null) {
            throw new BizException("O0404", "订单不存在");
        }
        return order;
    }

    private List<LyOrderItem> orderItems(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<LyOrderItem>().eq(LyOrderItem::getOrderId, orderId));
    }

    private LyOrder buildOrder(Long userId,
                               Integer orderType,
                               String orderNo,
                               BigDecimal totalAmount,
                               String receiverName,
                               String receiverPhone,
                               String receiverAddress,
                               String remark) {
        LyOrder order = new LyOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setOrderType(orderType);
        order.setStatus(STATUS_WAIT_PAY);
        order.setPayStatus(PAY_UNPAID);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setRemark(remark);
        return order;
    }

    private LyOrderItem buildOrderItem(String orderNo, ProductSkuVO sku, Integer quantity, BigDecimal amount) {
        LyOrderItem item = new LyOrderItem();
        item.setOrderNo(orderNo);
        item.setSpuId(sku.getSpuId());
        item.setSkuId(sku.getId());
        item.setSkuTitle(sku.getTitle());
        item.setSkuAttrsJson(sku.getAttrsJson());
        item.setPrice(sku.getPrice());
        item.setQuantity(quantity);
        item.setAmount(amount);
        return item;
    }

    private void saveItems(LyOrder order, List<LyOrderItem> items) {
        for (LyOrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
    }

    private OrderVO toVO(LyOrder order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setOrderType(order.getOrderType());
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

    private void safeReleaseNormalStock(Long skuId, Integer quantity) {
        StockChangeRequest request = new StockChangeRequest();
        request.setSkuId(skuId);
        request.setQuantity(quantity);
        try {
            Result<Void> result = productClient.releaseStock(request);
            if (result == null || !SUCCESS_CODE.equals(result.getCode())) {
                return;
            }
        } catch (RuntimeException ignored) {
        }
    }

    private String buildSeckillRemark(InternalCreateSeckillOrderRequest request) {
        return "SECKILL:" + request.getActivityId() + ':' + request.getActivitySkuId() + ':' + request.getSeckillRecordId();
    }

    private String nextOrderNo() {
        return "LY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private void lockStock(StockChangeRequest request) {
        Result<Void> result;
        try {
            result = productClient.lockStock(request);
        } catch (RuntimeException ex) {
            throw new BizException("O0500", "商品服务暂时不可用，请稍后重试");
        }
        if (result == null || !SUCCESS_CODE.equals(result.getCode())) {
            throw new BizException("O0500", resolveMessage(result, "商品服务暂时不可用，请稍后重试"));
        }
    }

    private void deductStock(StockChangeRequest request) {
        Result<Void> result;
        try {
            result = productClient.deductStock(request);
        } catch (RuntimeException ex) {
            throw new BizException("O0500", "商品服务暂时不可用，请稍后重试");
        }
        if (result == null || !SUCCESS_CODE.equals(result.getCode())) {
            throw new BizException("O0500", resolveMessage(result, "商品服务暂时不可用，请稍后重试"));
        }
    }

    private void confirmSeckillOrder(Long orderId) {
        Result<Void> result;
        try {
            result = seckillClient.confirmByOrderId(orderId);
        } catch (RuntimeException ex) {
            throw new BizException("O0500", "秒杀服务暂时不可用，请稍后重试");
        }
        if (result == null || !SUCCESS_CODE.equals(result.getCode())) {
            throw new BizException("O0500", resolveMessage(result, "秒杀服务暂时不可用，请稍后重试"));
        }
    }

    private void releaseSeckillOrder(Long orderId) {
        Result<Void> result;
        try {
            result = seckillClient.releaseByOrderId(orderId);
        } catch (RuntimeException ex) {
            throw new BizException("O0500", "秒杀服务暂时不可用，请稍后重试");
        }
        if (result == null || !SUCCESS_CODE.equals(result.getCode())) {
            throw new BizException("O0500", resolveMessage(result, "秒杀服务暂时不可用，请稍后重试"));
        }
    }

    private String resolveMessage(Result<?> result, String fallback) {
        if (result == null) {
            return fallback;
        }
        String message = result.getMessage();
        return message == null || message.isBlank() ? fallback : message;
    }
}
