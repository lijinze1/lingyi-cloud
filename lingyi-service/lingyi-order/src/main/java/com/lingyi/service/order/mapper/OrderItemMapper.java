package com.lingyi.service.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.service.order.entity.LyOrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<LyOrderItem> {
}
