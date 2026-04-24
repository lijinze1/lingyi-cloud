package com.lingyi.service.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.service.cart.entity.LyCartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<LyCartItem> {
}
