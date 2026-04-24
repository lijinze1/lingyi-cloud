package com.lingyi.service.cart.service;

import com.lingyi.service.cart.dto.CartAddRequest;
import com.lingyi.service.cart.dto.CartUpdateRequest;
import com.lingyi.service.cart.vo.CartItemVO;
import java.util.List;

public interface CartService {
    List<CartItemVO> list(Long userId);

    CartItemVO add(Long userId, CartAddRequest request);

    CartItemVO update(Long userId, Long itemId, CartUpdateRequest request);

    void remove(Long userId, Long itemId);

    void removeChecked(Long userId);
}
