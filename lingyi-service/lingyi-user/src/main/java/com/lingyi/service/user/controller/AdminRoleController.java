package com.lingyi.service.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingyi.common.web.domain.Result;
import com.lingyi.service.user.dto.AdminRolePermissionBindRequest;
import com.lingyi.service.user.dto.AdminRoleSaveRequest;
import com.lingyi.service.user.dto.AdminUserStatusRequest;
import com.lingyi.service.user.dto.UserPageQueryDTO;
import com.lingyi.service.user.security.RequirePermission;
import com.lingyi.service.user.service.RoleAdminService;
import com.lingyi.service.user.vo.AdminRoleVO;
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
@RequestMapping("/api/user/admin/roles")
public class AdminRoleController {

    private final RoleAdminService roleAdminService;

    public AdminRoleController(RoleAdminService roleAdminService) {
        this.roleAdminService = roleAdminService;
    }

    @GetMapping
    @RequirePermission("user:admin:role:page")
    public Result<IPage<AdminRoleVO>> page(UserPageQueryDTO queryDTO) {
        return Result.success(roleAdminService.page(queryDTO));
    }

    @GetMapping("/{roleId}")
    @RequirePermission("user:admin:role:detail")
    public Result<AdminRoleVO> detail(@PathVariable Long roleId) {
        return Result.success(roleAdminService.detail(roleId));
    }

    @PostMapping
    @RequirePermission("user:admin:role:create")
    public Result<AdminRoleVO> create(@Valid @RequestBody AdminRoleSaveRequest request) {
        return Result.success(roleAdminService.create(request));
    }

    @PutMapping("/{roleId}")
    @RequirePermission("user:admin:role:update")
    public Result<AdminRoleVO> update(@PathVariable Long roleId, @Valid @RequestBody AdminRoleSaveRequest request) {
        return Result.success(roleAdminService.update(roleId, request));
    }

    @PutMapping("/{roleId}/status")
    @RequirePermission("user:admin:role:status")
    public Result<Void> changeStatus(@PathVariable Long roleId, @Valid @RequestBody AdminUserStatusRequest request) {
        roleAdminService.changeStatus(roleId, request);
        return Result.success((Void) null);
    }

    @GetMapping("/{roleId}/permissions")
    @RequirePermission("user:admin:role:permissions:view")
    public Result<List<Long>> permissionIds(@PathVariable Long roleId) {
        return Result.success(roleAdminService.permissionIds(roleId));
    }

    @PutMapping("/{roleId}/permissions")
    @RequirePermission("user:admin:role:permissions:bind")
    public Result<Void> bindPermissions(@PathVariable Long roleId, @Valid @RequestBody AdminRolePermissionBindRequest request) {
        roleAdminService.bindPermissions(roleId, request);
        return Result.success((Void) null);
    }
}

