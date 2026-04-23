package com.lingyi.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.user.convert.PermissionConvert;
import com.lingyi.service.user.dto.AdminPermissionSaveRequest;
import com.lingyi.service.user.dto.AdminUserStatusRequest;
import com.lingyi.service.user.dto.UserPageQueryDTO;
import com.lingyi.service.user.entity.LyPermission;
import com.lingyi.service.user.service.PermissionAdminService;
import com.lingyi.service.user.util.SnowflakeIdGenerator;
import com.lingyi.service.user.mapper.PermissionMapper;
import com.lingyi.service.user.vo.AdminPermissionVO;
import org.springframework.stereotype.Service;

@Service
public class PermissionAdminServiceImpl implements PermissionAdminService {

    private final PermissionMapper permissionMapper;
    private final SnowflakeIdGenerator idGenerator;

    public PermissionAdminServiceImpl(PermissionMapper permissionMapper, SnowflakeIdGenerator idGenerator) {
        this.permissionMapper = permissionMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public IPage<AdminPermissionVO> page(UserPageQueryDTO queryDTO, String permType) {
        Page<LyPermission> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<LyPermission> wrapper = new LambdaQueryWrapper<LyPermission>()
                .eq(LyPermission::getIsDeleted, 0)
                .orderByDesc(LyPermission::getCreatedAt);
        if (permType != null && !permType.isBlank()) {
            wrapper.eq(LyPermission::getPermType, permType.trim());
        }
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isBlank()) {
            String keyword = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(LyPermission::getPermCode, keyword).or().like(LyPermission::getPermName, keyword));
        }
        return permissionMapper.selectPage(page, wrapper).convert(PermissionConvert::toAdminPermissionVO);
    }

    @Override
    public AdminPermissionVO detail(Long permissionId) {
        return PermissionConvert.toAdminPermissionVO(requirePermission(permissionId));
    }

    @Override
    public AdminPermissionVO create(AdminPermissionSaveRequest request) {
        ensureCodeAvailable(request.getPermCode().trim(), null);
        LyPermission permission = new LyPermission();
        permission.setId(idGenerator.nextId());
        permission.setPermCode(request.getPermCode().trim());
        permission.setPermName(request.getPermName().trim());
        permission.setPermType(request.getPermType().trim().toUpperCase());
        permission.setPath(blankToNull(request.getPath()));
        permission.setMethod(blankToNull(request.getMethod() == null ? null : request.getMethod().trim().toUpperCase()));
        permission.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        permission.setRemark(blankToNull(request.getRemark()));
        permission.setIsDeleted(0);
        permissionMapper.insert(permission);
        return detail(permission.getId());
    }

    @Override
    public AdminPermissionVO update(Long permissionId, AdminPermissionSaveRequest request) {
        LyPermission permission = requirePermission(permissionId);
        ensureCodeAvailable(request.getPermCode().trim(), permissionId);
        permission.setPermCode(request.getPermCode().trim());
        permission.setPermName(request.getPermName().trim());
        permission.setPermType(request.getPermType().trim().toUpperCase());
        permission.setPath(blankToNull(request.getPath()));
        permission.setMethod(blankToNull(request.getMethod() == null ? null : request.getMethod().trim().toUpperCase()));
        permission.setStatus(request.getStatus() == null ? permission.getStatus() : request.getStatus());
        permission.setRemark(blankToNull(request.getRemark()));
        permissionMapper.updateById(permission);
        return detail(permissionId);
    }

    @Override
    public void changeStatus(Long permissionId, AdminUserStatusRequest request) {
        LyPermission permission = requirePermission(permissionId);
        permission.setStatus(request.getStatus());
        permissionMapper.updateById(permission);
    }

    private LyPermission requirePermission(Long permissionId) {
        LyPermission permission = permissionMapper.selectById(permissionId);
        if (permission == null || Integer.valueOf(1).equals(permission.getIsDeleted())) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return permission;
    }

    private void ensureCodeAvailable(String permissionCode, Long currentPermissionId) {
        LyPermission exist = permissionMapper.selectOne(new LambdaQueryWrapper<LyPermission>()
                .eq(LyPermission::getPermCode, permissionCode)
                .eq(LyPermission::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (exist != null && !exist.getId().equals(currentPermissionId)) {
            throw new BizException(ErrorCode.PERMISSION_CODE_ALREADY_EXISTS);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
