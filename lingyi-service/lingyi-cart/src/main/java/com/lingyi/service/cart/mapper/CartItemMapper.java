package com.lingyi.service.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.service.cart.entity.LyCartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CartItemMapper extends BaseMapper<LyCartItem> {

    @Select("""
            SELECT id, user_id, sku_id, quantity, checked, price_snapshot, created_by, updated_by, created_at, updated_at, is_deleted
            FROM ly_cart_item
            WHERE user_id = #{userId} AND sku_id = #{skuId}
            LIMIT 1
            """)
    LyCartItem selectAnyByUserIdAndSkuId(@Param("userId") Long userId, @Param("skuId") Long skuId);

    @Update("""
            UPDATE ly_cart_item
            SET quantity = #{quantity},
                checked = 1,
                price_snapshot = #{priceSnapshot},
                updated_by = #{userId},
                is_deleted = 0
            WHERE id = #{id}
            """)
    int restoreDeletedItem(@Param("id") Long id,
                           @Param("userId") Long userId,
                           @Param("quantity") Integer quantity,
                           @Param("priceSnapshot") java.math.BigDecimal priceSnapshot);
}
