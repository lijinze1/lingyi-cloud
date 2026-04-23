package com.lingyi.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.user.convert.RoleConvert;
import com.lingyi.service.user.dto.AdminRolePermissionBindRequest;
import com.lingyi.service.user.dto.AdminRoleSaveRequest;
import com.lingyi.service.user.dto.AdminUserStatusRequest;
import com.lingyi.service.user.dto.UserPageQueryDTO;
import com.lingyi.service.user.entity.LyRole;
import com.lingyi.service.user.entity.LyRolePermission;
import com.lingyi.service.user.mapper.RoleMapper;
import com.lingyi.service.user.mapper.RolePermissionMapper;
import com.lingyi.service.user.service.RoleAdminService;
import com.lingyi.service.user.util.SnowflakeIdGenerator;
import com.lingyi.service.user.vo.AdminRoleVO;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleAdminServiceImpl implements RoleAdminService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final SnowflakeIdGenerator idGenerator;

    public RoleAdminServiceImpl(RoleMapper roleMapper,
                                RolePermissionMapper rolePermissionMapper,
                                SnowflakeIdGenerator idGenerator) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public IPage<AdminRoleVO> page(UserPageQueryDTO queryDTO) {
        Page<LyRole> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<LyRole> wrapper = new LambdaQueryWrapper<LyRole>()
                .eq(LyRole::getIsDeleted, 0)
                .orderByDesc(LyRole::getCreatedAt);
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isBlank()) {
            String keyword = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(LyRole::getRoleCode, keyword).or().like(LyRole::getRoleName, keyword));
        }
        return roleMapper.selectPage(page, wrapper).convert(RoleConvert::toAdminRoleVO);
    }

    @Override
    public AdminRoleVO detail(Long roleId) {
        return RoleConvert.toAdminRoleVO(requireRole(roleId));
    }

    @Override
    public AdminRoleVO create(AdminRoleSaveRequest request) {
        ensureRoleCodeAvailable(request.getRoleCode().trim(), null);
        LyRole role = new LyRole();
        role.setId(idGenerator.nextId());
        role.setRoleCode(request.getRoleCode().trim());
        role.setRoleName(request.getRoleName().trim());
        role.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        role.setRemark(request.getRemark());
        role.setIsDeleted(0);
        roleMapper.insert(role);
        return detail(role.getId());
    }

    @Override
    public AdminRoleVO update(Long roleId, AdminRoleSaveRequest request) {
        LyRole role = requireRole(roleId);
        ensureRoleCodeAvailable(request.getRoleCode().trim(), roleId);
        role.setRoleCode(request.getRoleCode().trim());
        role.setRoleName(request.getRoleName().trim());
        role.setStatus(request.getStatus() == null ? role.getStatus() : request.getStatus());
        role.setRemark(request.getRemark());
        roleMapper.updateById(role);
        return detail(roleId);
    }

    @Override
    public void changeStatus(Long roleId, AdminUserStatusRequest request) {
        LyRole role = requireRole(roleId);
        role.setStatus(request.getStatus());
        roleMapper.updateById(role);
    }

    @Override
    public List<Long> permissionIds(Long roleId) {
        requireRole(roleId);
        return rolePermissionMapper.selectList(new LambdaQueryWrapper<LyRolePermission>()
                .eq(LyRolePermission::getRoleId, roleId))
                .stream().map(LyRolePermission::getPermissionId).toList();
    }

    @Override
    @Transactional
    public void bindPermissions(Long roleId, AdminRolePermissionBindRequest request) {
        requireRole(roleId);
        rolePermissionMapper.deleteByRoleId(roleId);
        for (Long permissionId : request.getPermissionIds().stream().distinct().toList()) {
            LyRolePermission relation = new LyRolePermission();
            relation.setId(idGenerator.nextId());
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            relation.setIsDeleted(0);
            rolePermissionMapper.insert(relation);
        }
    }

    private LyRole requireRole(Long roleId) {
        LyRole role = roleMapper.selectById(roleId);
        if (role == null || Integer.valueOf(1).equals(role.getIsDeleted())) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return role;
    }

    private void ensureRoleCodeAvailable(String roleCode, Long currentRoleId) {
        LyRole exist = roleMapper.selectOne(new LambdaQueryWrapper<LyRole>()
                .eq(LyRole::getRoleCode, roleCode)
                .eq(LyRole::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (exist != null && !exist.getId().equals(currentRoleId)) {
            throw new BizException(ErrorCode.ROLE_CODE_ALREADY_EXISTS);
        }
    }
}
