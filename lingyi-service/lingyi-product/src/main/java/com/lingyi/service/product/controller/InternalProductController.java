package com.lingyi.service.product.controller;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.product.dto.StockChangeRequest;
import com.lingyi.service.product.service.ProductService;
import com.lingyi.service.product.vo.SkuVO;
import com.lingyi.service.product.vo.StockVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product/internal")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @GetMapping("/skus/{id}")
    public Result<SkuVO> sku(@PathVariable Long id) {
        return Result.success(productService.getSku(id, true));
    }

    @GetMapping("/stocks/{skuId}")
    public Result<StockVO> stock(@PathVariable Long skuId) {
        return Result.success(productService.getStock(skuId));
    }

    @PostMapping("/stocks/lock")
    public Result<Void> lock(@Valid @RequestBody StockChangeRequest request) {
        productService.lockStock(request);
        return Result.success(null);
    }

    @PostMapping("/stocks/release")
    public Result<Void> release(@Valid @RequestBody StockChangeRequest request) {
        productService.releaseStock(request);
        return Result.success(null);
    }

    @PostMapping("/stocks/deduct")
    public Result<Void> deduct(@Valid @RequestBody StockChangeRequest request) {
        productService.deductLockedStock(request);
        return Result.success(null);
    }
}
