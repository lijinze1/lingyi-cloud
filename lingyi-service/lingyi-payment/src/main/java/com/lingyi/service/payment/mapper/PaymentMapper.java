package com.lingyi.service.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.service.payment.entity.LyPayment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper extends BaseMapper<LyPayment> {
}
