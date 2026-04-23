package com.lingyi.service.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lingyi.common.web.domain.Result;
import com.lingyi.service.user.dto.AdminPermissionSaveRequest;
import com.lingyi.service.user.dto.AdminUserStatusRequest;
import com.lingyi.service.user.dto.UserPageQueryDTO;
import com.lingyi.service.user.security.RequirePermission;
import com.lingyi.service.user.service.AuthService;
import com.lingyi.service.user.service.PermissionAdminService;
import com.lingyi.service.user.vo.AdminPermissionVO;
import com.lingyi.service.user.vo.CurrentUserVO;
import com.lingyi.service.user.vo.PermissionSummaryVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/admin")
public class AdminPermissionController {

    private final PermissionAdminService permissionAdminService;
    private final AuthService authService;

    public AdminPermissionController(PermissionAdminService permissionAdminService, AuthService authService) {
        this.permissionAdminService = permissionAdminService;
        this.authService = authService;
    }

    @GetMapping("/permissions")
    @RequirePermission("user:admin:permission:page")
    public Result<IPage<AdminPermissionVO>> page(UserPageQueryDTO queryDTO,
                                                 @RequestParam(required = false) String permType) {
        return Result.success(permissionAdminService.page(queryDTO, permType));
    }

    @GetMapping("/permissions/{permissionId}")
    @RequirePermission("user:admin:permission:detail")
    public Result<AdminPermissionVO> detail(@PathVariable Long permissionId) {
        return Result.success(permissionAdminService.detail(permissionId));
    }

    @PostMapping("/permissions")
    @RequirePermission("user:admin:permission:create")
    public Result<AdminPermissionVO> create(@Valid @RequestBody AdminPermissionSaveRequest request) {
        return Result.success(permissionAdminService.create(request));
    }

    @PutMapping("/permissions/{permissionId}")
    @RequirePermission("user:admin:permission:update")
    public Result<AdminPermissionVO> update(@PathVariable Long permissionId,
                                            @Valid @RequestBody AdminPermissionSaveRequest request) {
        return Result.success(permissionAdminService.update(permissionId, request));
    }

    @PutMapping("/permissions/{permissionId}/status")
    @RequirePermission("user:admin:permission:status")
    public Result<Void> changeStatus(@PathVariable Long permissionId,
                                     @Valid @RequestBody AdminUserStatusRequest request) {
        permissionAdminService.changeStatus(permissionId, request);
        return Result.success((Void) null);
    }

    @GetMapping("/me")
    public Result<CurrentUserVO> currentAdmin(HttpServletRequest request) {
        return Result.success(authService.currentUser(request));
    }

    @GetMapping("/me/permissions")
    public Result<PermissionSummaryVO> currentPermissions(HttpServletRequest request) {
        return Result.success(authService.currentUser(request).permissions());
    }
}

