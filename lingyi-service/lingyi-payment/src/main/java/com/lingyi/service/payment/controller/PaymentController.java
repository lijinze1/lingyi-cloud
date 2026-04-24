package com.lingyi.service.payment.controller;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.payment.dto.PaymentCreateRequest;
import com.lingyi.service.payment.service.PaymentService;
import com.lingyi.service.payment.vo.PaymentVO;
import jakarta.validation.Valid;
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
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        return Result.success(Map.of("service", "lingyi-payment", "status", "UP"));
    }

    @PostMapping("/payments")
    public Result<PaymentVO> create(@RequestHeader("X-User-Id") Long userId,
                                    @Valid @RequestBody PaymentCreateRequest request) {
        return Result.success(paymentService.create(userId, request));
    }

    @PostMapping("/payments/{paymentNo}/mock-success")
    public Result<PaymentVO> mockSuccess(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable String paymentNo) {
        return Result.success(paymentService.mockPaySuccess(userId, paymentNo));
    }
}
