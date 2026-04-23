package com.lingyi.service.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingyi.service.user.dto.AdminPermissionSaveRequest;
import com.lingyi.service.user.dto.AdminUserStatusRequest;
import com.lingyi.service.user.dto.UserPageQueryDTO;
import com.lingyi.service.user.vo.AdminPermissionVO;

public interface PermissionAdminService {

    IPage<AdminPermissionVO> page(UserPageQueryDTO queryDTO, String permType);

    AdminPermissionVO detail(Long permissionId);

    AdminPermissionVO create(AdminPermissionSaveRequest request);

    AdminPermissionVO update(Long permissionId, AdminPermissionSaveRequest request);

    void changeStatus(Long permissionId, AdminUserStatusRequest request);
}
