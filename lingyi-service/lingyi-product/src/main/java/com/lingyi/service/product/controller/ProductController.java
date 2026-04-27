package com.lingyi.service.product.controller;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.product.dto.ProductPageQuery;
import com.lingyi.service.product.service.ProductService;
import com.lingyi.service.product.vo.CategoryVO;
import com.lingyi.service.product.vo.PageVO;
import com.lingyi.service.product.vo.ProductDetailVO;
import com.lingyi.service.product.vo.SkuVO;
import com.lingyi.service.product.vo.SpuVO;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        return Result.success(Map.of("service", "lingyi-product", "status", "UP"));
    }

    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.success(productService.listCategories());
    }

    @GetMapping("/spus")
    public Result<PageVO<SpuVO>> page(ProductPageQuery query) {
        return Result.success(productService.pageProducts(query, true));
    }

    @GetMapping("/spus/recommend")
    public Result<PageVO<SpuVO>> recommend(ProductPageQuery query) {
        query.setPageNo(Math.max(query.getPageNo(), 1));
        query.setPageSize(Math.min(Math.max(query.getPageSize(), 1), 12));
        return Result.success(productService.pageProducts(query, true));
    }

    @GetMapping("/spus/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id, true));
    }

    @GetMapping("/skus/{id}")
    public Result<SkuVO> sku(@PathVariable Long id) {
        return Result.success(productService.getSku(id, true));
    }
}
