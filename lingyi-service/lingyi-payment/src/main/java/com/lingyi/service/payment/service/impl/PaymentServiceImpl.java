package com.lingyi.service.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.domain.Result;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.payment.client.OrderClient;
import com.lingyi.service.payment.dto.OrderPaySuccessRequest;
import com.lingyi.service.payment.dto.PaymentCreateRequest;
import com.lingyi.service.payment.entity.LyPayment;
import com.lingyi.service.payment.mapper.PaymentMapper;
import com.lingyi.service.payment.service.PaymentService;
import com.lingyi.service.payment.vo.OrderSnapshotVO;
import com.lingyi.service.payment.vo.PaymentVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String SUCCESS_CODE = ErrorCode.SUCCESS.getCode();
    private static final int STATUS_WAIT_PAY = 0;
    private static final int STATUS_PAID = 1;

    private final PaymentMapper paymentMapper;
    private final OrderClient orderClient;

    @Override
    public PaymentVO create(Long userId, PaymentCreateRequest request) {
        OrderSnapshotVO order = loadOrder(request.getOrderNo());
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException("P0404", "订单不存在");
        }
        LyPayment exist = findPaymentByOrderNo(request.getOrderNo(), userId);
        if (exist != null) {
            return toVO(exist);
        }
        LyPayment payment = new LyPayment();
        payment.setPaymentNo(nextPaymentNo());
        payment.setOrderNo(request.getOrderNo());
        payment.setUserId(userId);
        payment.setAmount(order.getPayAmount());
        payment.setPayChannel(request.getPayChannel());
        payment.setPayStatus(STATUS_WAIT_PAY);
        try {
            paymentMapper.insert(payment);
        } catch (DataAccessException ex) {
            throw new BizException("P0500", "支付表 ly_payment 不可用，请检查支付服务数据库初始化");
        }
        return toVO(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO mockPaySuccess(Long userId, String paymentNo) {
        LyPayment payment = findPaymentByPaymentNo(paymentNo);
        if (payment == null || !Objects.equals(payment.getUserId(), userId)) {
            throw new BizException("P0404", "支付单不存在");
        }
        if (Objects.equals(payment.getPayStatus(), STATUS_PAID)) {
            return toVO(payment);
        }
        payment.setPayStatus(STATUS_PAID);
        payment.setPaidAt(LocalDateTime.now());
        try {
            paymentMapper.updateById(payment);
        } catch (DataAccessException ex) {
            throw new BizException("P0500", "支付表 ly_payment 不可用，请检查支付服务数据库初始化");
        }
        OrderPaySuccessRequest request = new OrderPaySuccessRequest();
        request.setPaymentNo(payment.getPaymentNo());
        notifyOrderPaySuccess(payment.getOrderNo(), request);
        return toVO(payment);
    }

    private LyPayment findPaymentByOrderNo(String orderNo, Long userId) {
        try {
            return paymentMapper.selectOne(new LambdaQueryWrapper<LyPayment>()
                    .eq(LyPayment::getOrderNo, orderNo)
                    .eq(LyPayment::getUserId, userId)
                    .last("LIMIT 1"));
        } catch (DataAccessException ex) {
            throw new BizException("P0500", "支付表 ly_payment 不可用，请检查支付服务数据库初始化");
        }
    }

    private LyPayment findPaymentByPaymentNo(String paymentNo) {
        try {
            return paymentMapper.selectOne(new LambdaQueryWrapper<LyPayment>()
                    .eq(LyPayment::getPaymentNo, paymentNo)
                    .last("LIMIT 1"));
        } catch (DataAccessException ex) {
            throw new BizException("P0500", "支付表 ly_payment 不可用，请检查支付服务数据库初始化");
        }
    }

    private OrderSnapshotVO loadOrder(String orderNo) {
        Result<OrderSnapshotVO> result;
        try {
            result = orderClient.getByOrderNo(orderNo);
        } catch (RuntimeException ex) {
            throw new BizException("P0500", "订单服务暂时不可用，请稍后重试");
        }
        if (result == null || !SUCCESS_CODE.equals(result.getCode())) {
            throw new BizException("P0500", resolveMessage(result, "订单服务暂时不可用，请稍后重试"));
        }
        return result.getData();
    }

    private void notifyOrderPaySuccess(String orderNo, OrderPaySuccessRequest request) {
        Result<Void> result;
        try {
            result = orderClient.paySuccess(orderNo, request);
        } catch (RuntimeException ex) {
            throw new BizException("P0500", "订单服务暂时不可用，请稍后重试");
        }
        if (result == null || !SUCCESS_CODE.equals(result.getCode())) {
            throw new BizException("P0500", resolveMessage(result, "订单服务暂时不可用，请稍后重试"));
        }
    }

    private PaymentVO toVO(LyPayment payment) {
        PaymentVO vo = new PaymentVO();
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setOrderNo(payment.getOrderNo());
        vo.setUserId(payment.getUserId());
        vo.setAmount(payment.getAmount());
        vo.setPayChannel(payment.getPayChannel());
        vo.setPayStatus(payment.getPayStatus());
        vo.setPaidAt(payment.getPaidAt());
        return vo;
    }

    private String nextPaymentNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private String resolveMessage(Result<?> result, String fallback) {
        if (result == null) {
            return fallback;
        }
        String message = result.getMessage();
        return message == null || message.isBlank() ? fallback : message;
    }
}
