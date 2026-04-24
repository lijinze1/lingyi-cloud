package com.lingyi.service.order.client;

import com.lingyi.common.web.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "lingyi-product", contextId = "orderProductClient")
public interface ProductClient {
    @GetMapping("/api/product/internal/skus/{id}")
    Result<ProductSkuVO> getSku(@PathVariable("id") Long id);

    @PostMapping("/api/product/internal/stocks/lock")
    Result<Void> lockStock(@RequestBody StockChangeRequest request);

    @PostMapping("/api/product/internal/stocks/release")
    Result<Void> releaseStock(@RequestBody StockChangeRequest request);

    @PostMapping("/api/product/internal/stocks/deduct")
    Result<Void> deductStock(@RequestBody StockChangeRequest request);
}
