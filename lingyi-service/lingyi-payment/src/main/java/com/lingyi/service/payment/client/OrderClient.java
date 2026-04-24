package com.lingyi.service.payment.client;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.payment.dto.OrderPaySuccessRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "lingyi-order", contextId = "paymentOrderClient")
public interface OrderClient {
    @PostMapping("/api/order/internal/orders/{orderNo}/pay-success")
    Result<Void> paySuccess(@PathVariable("orderNo") String orderNo, @RequestBody OrderPaySuccessRequest request);
}
