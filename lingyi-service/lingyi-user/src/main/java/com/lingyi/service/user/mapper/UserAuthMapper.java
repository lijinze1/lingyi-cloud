package com.lingyi.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.service.user.entity.LyUserAuth;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAuthMapper extends BaseMapper<LyUserAuth> {
}
