package com.lingyi.service.payment.service;

import com.lingyi.service.payment.dto.PaymentCreateRequest;
import com.lingyi.service.payment.vo.PaymentVO;

public interface PaymentService {
    PaymentVO create(Long userId, PaymentCreateRequest request);

    PaymentVO mockPaySuccess(Long userId, String paymentNo);
}
