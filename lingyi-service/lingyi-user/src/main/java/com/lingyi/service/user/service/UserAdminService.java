package com.lingyi.service.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingyi.service.user.dto.AdminUserPasswordResetRequest;
import com.lingyi.service.user.dto.AdminUserRoleBindRequest;
import com.lingyi.service.user.dto.AdminUserSaveRequest;
import com.lingyi.service.user.dto.AdminUserStatusRequest;
import com.lingyi.service.user.dto.UserPageQueryDTO;
import com.lingyi.service.user.vo.AdminUserVO;
import java.util.List;

public interface UserAdminService {

    IPage<AdminUserVO> page(UserPageQueryDTO queryDTO);

    AdminUserVO detail(Long userId);

    AdminUserVO create(AdminUserSaveRequest request);

    AdminUserVO update(Long userId, AdminUserSaveRequest request);

    void changeStatus(Long userId, AdminUserStatusRequest request);

    void resetPassword(Long userId, AdminUserPasswordResetRequest request);

    List<Long> roleIds(Long userId);

    void bindRoles(Long userId, AdminUserRoleBindRequest request);
}
