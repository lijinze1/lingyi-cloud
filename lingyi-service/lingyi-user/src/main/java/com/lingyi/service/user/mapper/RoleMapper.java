package com.lingyi.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.service.user.entity.LyRole;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleMapper extends BaseMapper<LyRole> {

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
