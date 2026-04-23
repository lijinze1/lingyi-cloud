package com.lingyi.service.user.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lingyi.service.user.entity.LyPermission;
import com.lingyi.service.user.entity.LyRole;
import com.lingyi.service.user.entity.LyRolePermission;
import com.lingyi.service.user.entity.LyUser;
import com.lingyi.service.user.entity.LyUserAuth;
import com.lingyi.service.user.entity.LyUserRole;
import com.lingyi.service.user.enums.AuthTypeEnum;
import com.lingyi.service.user.mapper.PermissionMapper;
import com.lingyi.service.user.mapper.RoleMapper;
import com.lingyi.service.user.mapper.RolePermissionMapper;
import com.lingyi.service.user.mapper.UserAuthMapper;
import com.lingyi.service.user.mapper.UserMapper;
import com.lingyi.service.user.mapper.UserRoleMapper;
import com.lingyi.service.user.util.SnowflakeIdGenerator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserBootstrapRunner implements ApplicationRunner {

    private static final long DEFAULT_ADMIN_USER_ID = 1000000000001L;
    private static final long DEFAULT_TEST_USER_ID = 1000000000002L;
    private static final long DEFAULT_ADMIN_ROLE_ID = 1000000000201L;
    private static final long DEFAULT_USER_ROLE_ID = 1000000000202L;

    private final BootstrapProperties bootstrapProperties;
    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserBootstrapRunner(BootstrapProperties bootstrapProperties,
                               UserMapper userMapper,
                               UserAuthMapper userAuthMapper,
                               RoleMapper roleMapper,
                               PermissionMapper permissionMapper,
                               UserRoleMapper userRoleMapper,
                               RolePermissionMapper rolePermissionMapper,
                               SnowflakeIdGenerator idGenerator) {
        this.bootstrapProperties = bootstrapProperties;
        this.userMapper = userMapper;
        this.userAuthMapper = userAuthMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!bootstrapProperties.isEnabled()) {
            return;
        }
        LyRole adminRole = ensureRole(DEFAULT_ADMIN_ROLE_ID, "ROLE_ADMIN", "后台管理员");
        LyRole userRole = ensureRole(DEFAULT_USER_ROLE_ID, "ROLE_USER", "普通用户");
        Map<String, Long> permissionIds = bootstrapProperties.getRbac().isSeed() ? ensurePermissions() : Map.of();
        if (!permissionIds.isEmpty()) {
            bindRolePermissions(adminRole.getId(), permissionIds.values().stream().toList());
        }
        LyUser admin = ensureAccount(
                DEFAULT_ADMIN_USER_ID,
                bootstrapProperties.getAdmin().getUsername(),
                bootstrapProperties.getAdmin().getNickname(),
                bootstrapProperties.getAdmin().getPassword(),
                true);
        LyUser user = ensureAccount(
                DEFAULT_TEST_USER_ID,
                bootstrapProperties.getUser().getUsername(),
                bootstrapProperties.getUser().getNickname(),
                bootstrapProperties.getUser().getPassword(),
                true);
        bindUserRole(admin.getId(), adminRole.getId());
        bindUserRole(user.getId(), userRole.getId());
    }

    private LyRole ensureRole(Long roleId, String roleCode, String roleName) {
        LyRole role = roleMapper.selectOne(new LambdaQueryWrapper<LyRole>()
                .eq(LyRole::getRoleCode, roleCode)
                .eq(LyRole::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (role != null) {
            role.setRoleName(roleName);
            role.setStatus(1);
            roleMapper.updateById(role);
            return role;
        }
        role = new LyRole();
        role.setId(roleId);
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setStatus(1);
        role.setIsDeleted(0);
        roleMapper.insert(role);
        return role;
    }

    private Map<String, Long> ensurePermissions() {
        List<PermissionSeed> seeds = List.of(
                new PermissionSeed("admin:menu:dashboard", "工作台", "MENU", "/dashboard", null),
                new PermissionSeed("admin:menu:users", "用户管理", "MENU", "/users", null),
                new PermissionSeed("admin:menu:roles", "角色管理", "MENU", "/roles", null),
                new PermissionSeed("admin:menu:permissions", "权限管理", "MENU", "/permissions", null),
                new PermissionSeed("admin:menu:prompts", "提示词管理", "MENU", "/prompts", null),
                new PermissionSeed("admin:menu:knowledge", "知识库管理", "MENU", "/knowledge", null),
                new PermissionSeed("user:admin:user:page", "分页查询用户", "API", "/api/user/admin/users", "GET"),
                new PermissionSeed("user:admin:user:detail", "查看用户详情", "API", "/api/user/admin/users/*", "GET"),
                new PermissionSeed("user:admin:user:create", "创建用户", "API", "/api/user/admin/users", "POST"),
                new PermissionSeed("user:admin:user:update", "更新用户", "API", "/api/user/admin/users/*", "PUT"),
                new PermissionSeed("user:admin:user:status", "更新用户状态", "API", "/api/user/admin/users/*/status", "PUT"),
                new PermissionSeed("user:admin:user:password", "重置用户密码", "API", "/api/user/admin/users/*/password/reset", "PUT"),
                new PermissionSeed("user:admin:user:roles:view", "查看用户角色", "API", "/api/user/admin/users/*/roles", "GET"),
                new PermissionSeed("user:admin:user:roles:bind", "绑定用户角色", "API", "/api/user/admin/users/*/roles", "PUT"),
                new PermissionSeed("user:admin:role:page", "分页查询角色", "API", "/api/user/admin/roles", "GET"),
                new PermissionSeed("user:admin:role:detail", "查看角色详情", "API", "/api/user/admin/roles/*", "GET"),
                new PermissionSeed("user:admin:role:create", "创建角色", "API", "/api/user/admin/roles", "POST"),
                new PermissionSeed("user:admin:role:update", "更新角色", "API", "/api/user/admin/roles/*", "PUT"),
                new PermissionSeed("user:admin:role:status", "更新角色状态", "API", "/api/user/admin/roles/*/status", "PUT"),
                new PermissionSeed("user:admin:role:permissions:view", "查看角色权限", "API", "/api/user/admin/roles/*/permissions", "GET"),
                new PermissionSeed("user:admin:role:permissions:bind", "绑定角色权限", "API", "/api/user/admin/roles/*/permissions", "PUT"),
                new PermissionSeed("user:admin:permission:page", "分页查询权限", "API", "/api/user/admin/permissions", "GET"),
                new PermissionSeed("user:admin:permission:detail", "查看权限详情", "API", "/api/user/admin/permissions/*", "GET"),
                new PermissionSeed("user:admin:permission:create", "创建权限", "API", "/api/user/admin/permissions", "POST"),
                new PermissionSeed("user:admin:permission:update", "更新权限", "API", "/api/user/admin/permissions/*", "PUT"),
                new PermissionSeed("user:admin:permission:status", "更新权限状态", "API", "/api/user/admin/permissions/*/status", "PUT")
        );
        LinkedHashMap<String, Long> result = new LinkedHashMap<>();
        for (PermissionSeed seed : seeds) {
            LyPermission permission = permissionMapper.selectOne(new LambdaQueryWrapper<LyPermission>()
                    .eq(LyPermission::getPermCode, seed.permCode())
                    .eq(LyPermission::getIsDeleted, 0)
                    .last("LIMIT 1"));
            if (permission == null) {
                permission = new LyPermission();
                permission.setId(idGenerator.nextId());
                permission.setPermCode(seed.permCode());
                permission.setPermName(seed.permName());
                permission.setPermType(seed.permType());
                permission.setPath(seed.path());
                permission.setMethod(seed.method());
                permission.setStatus(1);
                permission.setIsDeleted(0);
                permissionMapper.insert(permission);
            } else {
                permission.setPermName(seed.permName());
                permission.setPermType(seed.permType());
                permission.setPath(seed.path());
                permission.setMethod(seed.method());
                permission.setStatus(1);
                permissionMapper.updateById(permission);
            }
            result.put(seed.permCode(), permission.getId());
        }
        return result;
    }

    private LyUser ensureAccount(Long defaultUserId, String username, String nickname, String rawPassword, boolean enable) {
        LyUser user = userMapper.selectOne(new LambdaQueryWrapper<LyUser>()
                .and(w -> w.eq(LyUser::getUsername, username).or().eq(LyUser::getPhone, username))
                .eq(LyUser::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (user == null) {
            user = new LyUser();
            user.setId(defaultUserId);
            user.setUsername(username);
            if (username != null && username.matches("^1\\d{10}$")) {
                user.setPhone(username);
            }
            user.setNickname((nickname == null || nickname.isBlank()) ? username : nickname.trim());
            user.setStatus(enable ? 1 : 0);
            user.setIsDeleted(0);
            userMapper.insert(user);
        } else {
            user.setUsername(username);
            if (username != null && username.matches("^1\\d{10}$")) {
                user.setPhone(username);
            }
            user.setNickname((nickname == null || nickname.isBlank()) ? username : nickname.trim());
            user.setStatus(enable ? 1 : 0);
            userMapper.updateById(user);
        }
        ensurePasswordAuth(user.getId(), username, rawPassword);
        return user;
    }

    private void ensurePasswordAuth(Long userId, String authKey, String rawPassword) {
        LyUserAuth auth = userAuthMapper.selectOne(new LambdaQueryWrapper<LyUserAuth>()
                .eq(LyUserAuth::getUserId, userId)
                .eq(LyUserAuth::getAuthType, AuthTypeEnum.PASSWORD.name())
                .eq(LyUserAuth::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (auth == null) {
            auth = new LyUserAuth();
            auth.setId(idGenerator.nextId());
            auth.setUserId(userId);
            auth.setAuthType(AuthTypeEnum.PASSWORD.name());
            auth.setAuthKey(authKey);
            auth.setStatus(1);
            auth.setIsDeleted(0);
            auth.setAuthSecret(passwordEncoder.encode(rawPassword));
            userAuthMapper.insert(auth);
            return;
        }
        auth.setAuthKey(authKey);
        auth.setAuthSecret(passwordEncoder.encode(rawPassword));
        auth.setStatus(1);
        userAuthMapper.updateById(auth);
    }

    private void bindUserRole(Long userId, Long roleId) {
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<LyUserRole>()
                .eq(LyUserRole::getUserId, userId)
                .eq(LyUserRole::getRoleId, roleId));
        if (count != null && count > 0) {
            return;
        }
        LyUserRole relation = new LyUserRole();
        relation.setId(idGenerator.nextId());
        relation.setUserId(userId);
        relation.setRoleId(roleId);
        relation.setIsDeleted(0);
        userRoleMapper.insert(relation);
    }

    private void bindRolePermissions(Long roleId, List<Long> permissionIds) {
        for (Long permissionId : permissionIds) {
            Long count = rolePermissionMapper.selectCount(new LambdaQueryWrapper<LyRolePermission>()
                    .eq(LyRolePermission::getRoleId, roleId)
                    .eq(LyRolePermission::getPermissionId, permissionId));
            if (count != null && count > 0) {
                continue;
            }
            LyRolePermission relation = new LyRolePermission();
            relation.setId(idGenerator.nextId());
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            relation.setIsDeleted(0);
            rolePermissionMapper.insert(relation);
        }
    }

    private record PermissionSeed(String permCode, String permName, String permType, String path, String method) {
    }
}