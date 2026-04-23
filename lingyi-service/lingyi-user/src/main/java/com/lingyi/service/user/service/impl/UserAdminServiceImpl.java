package com.lingyi.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.user.convert.UserConvert;
import com.lingyi.service.user.dto.AdminUserPasswordResetRequest;
import com.lingyi.service.user.dto.AdminUserRoleBindRequest;
import com.lingyi.service.user.dto.AdminUserSaveRequest;
import com.lingyi.service.user.dto.AdminUserStatusRequest;
import com.lingyi.service.user.dto.UserPageQueryDTO;
import com.lingyi.service.user.entity.LyUser;
import com.lingyi.service.user.entity.LyUserAuth;
import com.lingyi.service.user.entity.LyUserRole;
import com.lingyi.service.user.enums.AuthTypeEnum;
import com.lingyi.service.user.mapper.RoleMapper;
import com.lingyi.service.user.mapper.UserAuthMapper;
import com.lingyi.service.user.mapper.UserMapper;
import com.lingyi.service.user.mapper.UserRoleMapper;
import com.lingyi.service.user.service.UserAdminService;
import com.lingyi.service.user.util.SnowflakeIdGenerator;
import com.lingyi.service.user.vo.AdminUserVO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminServiceImpl implements UserAdminService {

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserAdminServiceImpl(UserMapper userMapper,
                                UserAuthMapper userAuthMapper,
                                UserRoleMapper userRoleMapper,
                                RoleMapper roleMapper,
                                SnowflakeIdGenerator idGenerator) {
        this.userMapper = userMapper;
        this.userAuthMapper = userAuthMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public IPage<AdminUserVO> page(UserPageQueryDTO queryDTO) {
        Page<LyUser> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<LyUser> wrapper = new LambdaQueryWrapper<LyUser>()
                .eq(LyUser::getIsDeleted, 0)
                .orderByDesc(LyUser::getCreatedAt);
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isBlank()) {
            String keyword = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(LyUser::getUsername, keyword)
                    .or().like(LyUser::getNickname, keyword)
                    .or().like(LyUser::getPhone, keyword));
        }
        IPage<LyUser> userPage = userMapper.selectPage(page, wrapper);
        return userPage.convert(user -> UserConvert.toAdminUserVO(user, roleMapper.selectRoleCodesByUserId(user.getId())));
    }

    @Override
    public AdminUserVO detail(Long userId) {
        LyUser user = requireUser(userId);
        return UserConvert.toAdminUserVO(user, roleMapper.selectRoleCodesByUserId(userId));
    }

    @Override
    @Transactional
    public AdminUserVO create(AdminUserSaveRequest request) {
        String phone = request.getPhone().trim();
        ensurePhoneAvailable(phone, null);
        LyUser user = new LyUser();
        user.setId(idGenerator.nextId());
        user.setUsername(phone);
        user.setPhone(phone);
        user.setNickname(resolveNickname(request.getNickname(), phone));
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        user.setIsDeleted(0);
        userMapper.insert(user);
        return detail(user.getId());
    }

    @Override
    @Transactional
    public AdminUserVO update(Long userId, AdminUserSaveRequest request) {
        LyUser user = requireUser(userId);
        String phone = request.getPhone().trim();
        ensurePhoneAvailable(phone, userId);
        user.setUsername(phone);
        user.setPhone(phone);
        user.setNickname(resolveNickname(request.getNickname(), phone));
        user.setStatus(request.getStatus() == null ? user.getStatus() : request.getStatus());
        userMapper.updateById(user);

        LyUserAuth passwordAuth = userAuthMapper.selectOne(new LambdaQueryWrapper<LyUserAuth>()
                .eq(LyUserAuth::getUserId, userId)
                .eq(LyUserAuth::getAuthType, AuthTypeEnum.PASSWORD.name())
                .eq(LyUserAuth::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (passwordAuth != null && !phone.equals(passwordAuth.getAuthKey())) {
            passwordAuth.setAuthKey(phone);
            userAuthMapper.updateById(passwordAuth);
        }
        return detail(userId);
    }

    @Override
    public void changeStatus(Long userId, AdminUserStatusRequest request) {
        requireUser(userId);
        userMapper.update(null, new LambdaUpdateWrapper<LyUser>()
                .eq(LyUser::getId, userId)
                .set(LyUser::getStatus, request.getStatus()));
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, AdminUserPasswordResetRequest request) {
        LyUser user = requireUser(userId);
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
            auth.setAuthKey(user.getPhone() != null ? user.getPhone() : user.getUsername());
            auth.setStatus(1);
            auth.setIsDeleted(0);
            auth.setAuthSecret(passwordEncoder.encode(request.getPassword()));
            userAuthMapper.insert(auth);
            return;
        }
        auth.setAuthSecret(passwordEncoder.encode(request.getPassword()));
        auth.setStatus(1);
        auth.setAuthKey(user.getPhone() != null ? user.getPhone() : user.getUsername());
        userAuthMapper.updateById(auth);
    }

    @Override
    public List<Long> roleIds(Long userId) {
        requireUser(userId);
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    @Transactional
    public void bindRoles(Long userId, AdminUserRoleBindRequest request) {
        requireUser(userId);
        userRoleMapper.deleteByUserId(userId);
        for (Long roleId : request.getRoleIds().stream().distinct().collect(Collectors.toList())) {
            LyUserRole relation = new LyUserRole();
            relation.setId(idGenerator.nextId());
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relation.setIsDeleted(0);
            userRoleMapper.insert(relation);
        }
    }

    private LyUser requireUser(Long userId) {
        LyUser user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return user;
    }

    private void ensurePhoneAvailable(String phone, Long currentUserId) {
        LyUser exist = userMapper.selectOne(new LambdaQueryWrapper<LyUser>()
                .eq(LyUser::getPhone, phone)
                .eq(LyUser::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (exist != null && !exist.getId().equals(currentUserId)) {
            throw new BizException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
    }

    private String resolveNickname(String nickname, String phone) {
        return nickname == null || nickname.isBlank() ? "用户" + phone.substring(phone.length() - 4) : nickname.trim();
    }
}
