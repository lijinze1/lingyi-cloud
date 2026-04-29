package com.lingyi.service.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.domain.Result;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.cart.client.ProductClient;
import com.lingyi.service.cart.client.ProductSkuVO;
import com.lingyi.service.cart.dto.CartAddRequest;
import com.lingyi.service.cart.dto.CartUpdateRequest;
import com.lingyi.service.cart.entity.LyCartItem;
import com.lingyi.service.cart.mapper.CartItemMapper;
import com.lingyi.service.cart.service.CartService;
import com.lingyi.service.cart.vo.CartItemVO;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final String SUCCESS_CODE = ErrorCode.SUCCESS.getCode();

    private final CartItemMapper cartItemMapper;
    private final ProductClient productClient;

    @Override
    public List<CartItemVO> list(Long userId) {
        return cartItemMapper.selectList(new LambdaQueryWrapper<LyCartItem>()
                        .eq(LyCartItem::getUserId, userId)
                        .orderByDesc(LyCartItem::getUpdatedAt))
                .stream()
                .map(item -> toVO(item, requireSku(item.getSkuId())))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemVO add(Long userId, CartAddRequest request) {
        ProductSkuVO sku = requireSku(request.getSkuId());
        if (sku.getStockAvailable() != null && sku.getStockAvailable() < request.getQuantity()) {
            throw new BizException("C0405", "库存不足，无法加入购物车");
        }
        LyCartItem item = cartItemMapper.selectOne(new LambdaQueryWrapper<LyCartItem>()
                .eq(LyCartItem::getUserId, userId)
                .eq(LyCartItem::getSkuId, request.getSkuId())
                .last("LIMIT 1"));
        if (item == null) {
            item = cartItemMapper.selectAnyByUserIdAndSkuId(userId, request.getSkuId());
            if (item != null) {
                cartItemMapper.restoreDeletedItem(item.getId(), userId, request.getQuantity(), sku.getPrice());
                item.setQuantity(request.getQuantity());
                item.setChecked(1);
                item.setPriceSnapshot(sku.getPrice());
                item.setIsDeleted(0);
            } else {
                item = new LyCartItem();
                item.setUserId(userId);
                item.setSkuId(request.getSkuId());
                item.setQuantity(request.getQuantity());
                item.setChecked(1);
                item.setPriceSnapshot(sku.getPrice());
                cartItemMapper.insert(item);
            }
        } else {
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setChecked(1);
            item.setPriceSnapshot(sku.getPrice());
            cartItemMapper.updateById(item);
        }
        return toVO(item, sku);
    }

    @Override
    public CartItemVO update(Long userId, Long itemId, CartUpdateRequest request) {
        LyCartItem item = requireItem(userId, itemId);
        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }
        if (request.getChecked() != null) {
            item.setChecked(request.getChecked());
        }
        cartItemMapper.updateById(item);
        return toVO(item, requireSku(item.getSkuId()));
    }

    @Override
    public void remove(Long userId, Long itemId) {
        LyCartItem item = requireItem(userId, itemId);
        cartItemMapper.deleteById(item.getId());
    }

    @Override
    public void removeChecked(Long userId) {
        cartItemMapper.selectList(new LambdaQueryWrapper<LyCartItem>()
                        .eq(LyCartItem::getUserId, userId)
                        .eq(LyCartItem::getChecked, 1))
                .forEach(item -> cartItemMapper.deleteById(item.getId()));
    }

    private CartItemVO toVO(LyCartItem item, ProductSkuVO sku) {
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId() == null ? null : String.valueOf(item.getId()));
        vo.setSkuId(item.getSkuId() == null ? null : String.valueOf(item.getSkuId()));
        vo.setSpuId(sku.getSpuId() == null ? null : String.valueOf(sku.getSpuId()));
        vo.setTitle(sku.getTitle());
        vo.setAttrsJson(sku.getAttrsJson());
        vo.setMainImage(sku.getMainImage());
        vo.setPrice(sku.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setChecked(item.getChecked());
        vo.setStockAvailable(sku.getStockAvailable());
        return vo;
    }

    private LyCartItem requireItem(Long userId, Long itemId) {
        LyCartItem item = cartItemMapper.selectById(itemId);
        if (item == null || !Objects.equals(item.getUserId(), userId)) {
            throw new BizException("C0404", "购物车商品不存在");
        }
        return item;
    }

    private ProductSkuVO requireSku(Long skuId) {
        Result<ProductSkuVO> result;
        try {
            result = productClient.getSku(skuId);
        } catch (RuntimeException ex) {
            throw new BizException("C0500", "商品服务暂时不可用，请稍后重试");
        }
        if (result == null || !SUCCESS_CODE.equals(result.getCode())) {
            throw new BizException("C0500", resolveMessage(result, "商品服务暂时不可用，请稍后重试"));
        }
        ProductSkuVO sku = result.getData();
        if (sku == null) {
            throw new BizException("C0404", "商品不存在或已下架");
        }
        return sku;
    }

    private String resolveMessage(Result<?> result, String fallback) {
        if (result == null) {
            return fallback;
        }
        String message = result.getMessage();
        return message == null || message.isBlank() ? fallback : message;
    }
}
