package com.lingyi.service.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.service.seckill.entity.LyActivitySku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ActivitySkuMapper extends BaseMapper<LyActivitySku> {
    @Update("UPDATE ly_activity_sku SET stock_available = stock_available - #{quantity}, stock_locked = stock_locked + #{quantity}, version = version + 1 WHERE id = #{id} AND status = 1 AND stock_available >= #{quantity} AND is_deleted = 0")
    int lockForOrder(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("UPDATE ly_activity_sku SET stock_locked = stock_locked - #{quantity}, version = version + 1 WHERE id = #{id} AND stock_locked >= #{quantity} AND is_deleted = 0")
    int confirmPaid(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("UPDATE ly_activity_sku SET stock_available = stock_available + #{quantity}, stock_locked = stock_locked - #{quantity}, version = version + 1 WHERE id = #{id} AND stock_locked >= #{quantity} AND is_deleted = 0")
    int releaseLocked(@Param("id") Long id, @Param("quantity") Integer quantity);
}