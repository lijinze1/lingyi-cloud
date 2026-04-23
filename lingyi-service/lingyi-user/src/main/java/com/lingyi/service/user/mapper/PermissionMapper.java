package com.lingyi.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingyi.service.user.entity.LyPermission;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PermissionMapper extends BaseMapper<LyPermission> {

    List<LyPermission> selectPermissionsByUserId(@Param("userId") Long userId);

    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
}
