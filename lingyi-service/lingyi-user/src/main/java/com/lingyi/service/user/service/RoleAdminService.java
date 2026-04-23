package com.lingyi.service.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingyi.service.user.dto.AdminRolePermissionBindRequest;
import com.lingyi.service.user.dto.AdminRoleSaveRequest;
import com.lingyi.service.user.dto.AdminUserStatusRequest;
import com.lingyi.service.user.dto.UserPageQueryDTO;
import com.lingyi.service.user.vo.AdminRoleVO;
import java.util.List;

public interface RoleAdminService {

    IPage<AdminRoleVO> page(UserPageQueryDTO queryDTO);

    AdminRoleVO detail(Long roleId);

    AdminRoleVO create(AdminRoleSaveRequest request);

    AdminRoleVO update(Long roleId, AdminRoleSaveRequest request);

    void changeStatus(Long roleId, AdminUserStatusRequest request);

    List<Long> permissionIds(Long roleId);

    void bindPermissions(Long roleId, AdminRolePermissionBindRequest request);
}
