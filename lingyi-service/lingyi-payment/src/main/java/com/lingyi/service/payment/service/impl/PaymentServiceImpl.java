package com.lingyi.service.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.payment.client.OrderClient;
import com.lingyi.service.payment.dto.OrderPaySuccessRequest;
import com.lingyi.service.payment.dto.PaymentCreateRequest;
import com.lingyi.service.payment.entity.LyPayment;
import com.lingyi.service.payment.mapper.PaymentMapper;
import com.lingyi.service.payment.service.PaymentService;
import com.lingyi.service.payment.vo.PaymentVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final int STATUS_WAIT_PAY = 0;
    private static final int STATUS_PAID = 1;

    private final PaymentMapper paymentMapper;
    private final OrderClient orderClient;

    @Override
    public PaymentVO create(Long userId, PaymentCreateRequest request) {
        LyPayment exist = paymentMapper.selectOne(new LambdaQueryWrapper<LyPayment>()
                .eq(LyPayment::getOrderNo, request.getOrderNo())
                .eq(LyPayment::getUserId, userId)
                .last("LIMIT 1"));
        if (exist != null) {
            return toVO(exist);
        }
        LyPayment payment = new LyPayment();
        payment.setPaymentNo(nextPaymentNo());
        payment.setOrderNo(request.getOrderNo());
        payment.setUserId(userId);
        payment.setAmount(request.getAmount());
        payment.setPayChannel(request.getPayChannel());
        payment.setPayStatus(STATUS_WAIT_PAY);
        paymentMapper.insert(payment);
        return toVO(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO mockPaySuccess(Long userId, String paymentNo) {
        LyPayment payment = paymentMapper.selectOne(new LambdaQueryWrapper<LyPayment>()
                .eq(LyPayment::getPaymentNo, paymentNo)
                .last("LIMIT 1"));
        if (payment == null || !Objects.equals(payment.getUserId(), userId)) {
            throw new BizException("P0404", "支付单不存在");
        }
        if (Objects.equals(payment.getPayStatus(), STATUS_PAID)) {
            return toVO(payment);
        }
        payment.setPayStatus(STATUS_PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.updateById(payment);
        OrderPaySuccessRequest request = new OrderPaySuccessRequest();
        request.setPaymentNo(payment.getPaymentNo());
        orderClient.paySuccess(payment.getOrderNo(), request);
        return toVO(payment);
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
}
