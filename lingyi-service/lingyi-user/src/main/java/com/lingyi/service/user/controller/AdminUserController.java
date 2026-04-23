package com.lingyi.service.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingyi.common.web.domain.Result;
import com.lingyi.service.user.dto.AdminUserPasswordResetRequest;
import com.lingyi.service.user.dto.AdminUserRoleBindRequest;
import com.lingyi.service.user.dto.AdminUserSaveRequest;
import com.lingyi.service.user.dto.AdminUserStatusRequest;
import com.lingyi.service.user.dto.UserPageQueryDTO;
import com.lingyi.service.user.security.RequirePermission;
import com.lingyi.service.user.service.UserAdminService;
import com.lingyi.service.user.vo.AdminUserVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/admin/users")
public class AdminUserController {

    private final UserAdminService userAdminService;

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    @RequirePermission("user:admin:user:page")
    public Result<IPage<AdminUserVO>> page(UserPageQueryDTO queryDTO) {
        return Result.success(userAdminService.page(queryDTO));
    }

    @GetMapping("/{userId}")
    @RequirePermission("user:admin:user:detail")
    public Result<AdminUserVO> detail(@PathVariable Long userId) {
        return Result.success(userAdminService.detail(userId));
    }

    @PostMapping
    @RequirePermission("user:admin:user:create")
    public Result<AdminUserVO> create(@Valid @RequestBody AdminUserSaveRequest request) {
        return Result.success(userAdminService.create(request));
    }

    @PutMapping("/{userId}")
    @RequirePermission("user:admin:user:update")
    public Result<AdminUserVO> update(@PathVariable Long userId, @Valid @RequestBody AdminUserSaveRequest request) {
        return Result.success(userAdminService.update(userId, request));
    }

    @PutMapping("/{userId}/status")
    @RequirePermission("user:admin:user:status")
    public Result<Void> changeStatus(@PathVariable Long userId, @Valid @RequestBody AdminUserStatusRequest request) {
        userAdminService.changeStatus(userId, request);
        return Result.success((Void) null);
    }

    @PutMapping("/{userId}/password/reset")
    @RequirePermission("user:admin:user:password")
    public Result<Void> resetPassword(@PathVariable Long userId, @Valid @RequestBody AdminUserPasswordResetRequest request) {
        userAdminService.resetPassword(userId, request);
        return Result.success((Void) null);
    }

    @GetMapping("/{userId}/roles")
    @RequirePermission("user:admin:user:roles:view")
    public Result<List<Long>> roleIds(@PathVariable Long userId) {
        return Result.success(userAdminService.roleIds(userId));
    }

    @PutMapping("/{userId}/roles")
    @RequirePermission("user:admin:user:roles:bind")
    public Result<Void> bindRoles(@PathVariable Long userId, @Valid @RequestBody AdminUserRoleBindRequest request) {
        userAdminService.bindRoles(userId, request);
        return Result.success((Void) null);
    }
}

