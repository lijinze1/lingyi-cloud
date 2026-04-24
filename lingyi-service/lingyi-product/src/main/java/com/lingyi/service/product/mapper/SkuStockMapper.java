package com.lingyi.service.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.service.product.entity.LySkuStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SkuStockMapper extends BaseMapper<LySkuStock> {

    @Update("""
            UPDATE ly_sku_stock
            SET stock_available = stock_available - #{quantity},
                stock_locked = stock_locked + #{quantity},
                version = version + 1,
                updated_at = NOW()
            WHERE sku_id = #{skuId}
              AND is_deleted = 0
              AND stock_available >= #{quantity}
            """)
    int lockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Update("""
            UPDATE ly_sku_stock
            SET stock_available = stock_available + #{quantity},
                stock_locked = stock_locked - #{quantity},
                version = version + 1,
                updated_at = NOW()
            WHERE sku_id = #{skuId}
              AND is_deleted = 0
              AND stock_locked >= #{quantity}
            """)
    int releaseStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    @Update("""
            UPDATE ly_sku_stock
            SET stock_locked = stock_locked - #{quantity},
                stock_total = stock_total - #{quantity},
                version = version + 1,
                updated_at = NOW()
            WHERE sku_id = #{skuId}
              AND is_deleted = 0
              AND stock_locked >= #{quantity}
              AND stock_total >= #{quantity}
            """)
    int deductLockedStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
