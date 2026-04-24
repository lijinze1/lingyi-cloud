package com.lingyi.service.cart.controller;

import com.lingyi.common.web.domain.Result;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.cart.dto.CartAddRequest;
import com.lingyi.service.cart.dto.CartUpdateRequest;
import com.lingyi.service.cart.service.CartService;
import com.lingyi.service.cart.vo.CartItemVO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        return Result.success(Map.of("service", "lingyi-cart", "status", "UP"));
    }

    @GetMapping("/items")
    public Result<List<CartItemVO>> list(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(cartService.list(userId));
    }

    @PostMapping("/items")
    public Result<CartItemVO> add(@RequestHeader("X-User-Id") Long userId,
                                  @Valid @RequestBody CartAddRequest request) {
        return Result.success(cartService.add(userId, request));
    }

    @PutMapping("/items/{id}")
    public Result<CartItemVO> update(@RequestHeader("X-User-Id") Long userId,
                                     @PathVariable Long id,
                                     @Valid @RequestBody CartUpdateRequest request) {
        return Result.success(cartService.update(userId, id, request));
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> remove(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        cartService.remove(userId, id);
        return Result.success(null);
    }

    @DeleteMapping("/items/checked")
    public Result<Void> removeChecked(@RequestHeader("X-User-Id") Long userId) {
        cartService.removeChecked(userId);
        return Result.success(null);
    }
}
