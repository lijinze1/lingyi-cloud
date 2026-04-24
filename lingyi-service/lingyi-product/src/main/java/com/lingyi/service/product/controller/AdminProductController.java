package com.lingyi.service.product.controller;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.product.dto.CategorySaveRequest;
import com.lingyi.service.product.dto.ProductPageQuery;
import com.lingyi.service.product.dto.SpuSaveRequest;
import com.lingyi.service.product.dto.StatusUpdateRequest;
import com.lingyi.service.product.dto.StockSetRequest;
import com.lingyi.service.product.service.ProductService;
import com.lingyi.service.product.vo.CategoryVO;
import com.lingyi.service.product.vo.PageVO;
import com.lingyi.service.product.vo.ProductDetailVO;
import com.lingyi.service.product.vo.SpuVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.success(productService.listCategories());
    }

    @PostMapping("/categories")
    public Result<CategoryVO> saveCategory(@Valid @RequestBody CategorySaveRequest request) {
        return Result.success(productService.saveCategory(request));
    }

    @GetMapping("/spus")
    public Result<PageVO<SpuVO>> page(ProductPageQuery query) {
        return Result.success(productService.pageProducts(query, false));
    }

    @GetMapping("/spus/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id, false));
    }

    @PostMapping("/spus")
    public Result<ProductDetailVO> saveSpu(@Valid @RequestBody SpuSaveRequest request) {
        return Result.success(productService.saveSpu(request));
    }

    @PutMapping("/spus/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        productService.updateSpuStatus(id, request.getStatus());
        return Result.success(null);
    }

    @PutMapping("/stocks")
    public Result<Void> setStock(@Valid @RequestBody StockSetRequest request) {
        productService.setStock(request);
        return Result.success(null);
    }
}
