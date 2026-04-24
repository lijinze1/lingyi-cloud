package com.lingyi.service.cart.client;

import com.lingyi.common.web.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "lingyi-product", contextId = "cartProductClient")
public interface ProductClient {
    @GetMapping("/api/product/internal/skus/{id}")
    Result<ProductSkuVO> getSku(@PathVariable("id") Long id);
}
