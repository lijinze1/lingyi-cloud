package com.lingyi.service.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.service.seckill.entity.LySeckillRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface SeckillRecordMapper extends BaseMapper<LySeckillRecord> {

    @Select("""
            SELECT id, activity_id, sku_id, user_id, order_id, status, created_by, updated_by, created_at, updated_at, is_deleted
            FROM ly_seckill_record
            WHERE activity_id = #{activityId} AND sku_id = #{skuId} AND user_id = #{userId}
            LIMIT 1
            """)
    LySeckillRecord selectAnyByActivitySkuUser(@Param("activityId") Long activityId,
                                               @Param("skuId") Long skuId,
                                               @Param("userId") Long userId);

    @Update("""
            UPDATE ly_seckill_record
            SET order_id = NULL,
                status = #{status},
                updated_by = #{userId},
                is_deleted = 0
            WHERE id = #{id}
            """)
    int restoreDeletedRecord(@Param("id") Long id,
                             @Param("userId") Long userId,
                             @Param("status") Integer status);
}
