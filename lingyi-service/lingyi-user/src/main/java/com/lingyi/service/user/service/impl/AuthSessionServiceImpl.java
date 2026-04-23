package com.lingyi.service.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.user.config.AuthProperties;
import com.lingyi.service.user.config.SsoProperties;
import com.lingyi.service.user.entity.LyPermission;
import com.lingyi.service.user.mapper.PermissionMapper;
import com.lingyi.service.user.mapper.RoleMapper;
import com.lingyi.service.user.security.LoginUserContext;
import com.lingyi.service.user.service.AuthSessionService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthSessionServiceImpl implements AuthSessionService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;
    private final SsoProperties ssoProperties;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public AuthSessionServiceImpl(StringRedisTemplate stringRedisTemplate,
                                  ObjectMapper objectMapper,
                                  AuthProperties authProperties,
                                  SsoProperties ssoProperties,
                                  RoleMapper roleMapper,
                                  PermissionMapper permissionMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.authProperties = authProperties;
        this.ssoProperties = ssoProperties;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public SessionLoginResult createSession(Long userId, String username, String nickname) {
        SessionSnapshotValue snapshotValue = buildSnapshot(userId, username, nickname);
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        Duration ttl = Duration.ofSeconds(authProperties.getTokenExpireSeconds());
        String sessionKey = buildSessionKey(sessionId);
        String userSessionKey = buildUserSessionKey(userId);
        try {
            if (ssoProperties.isSingleSession()) {
                String oldSessionId = stringRedisTemplate.opsForValue().get(userSessionKey);
                if (oldSessionId != null && !oldSessionId.isBlank()) {
                    stringRedisTemplate.delete(buildSessionKey(oldSessionId));
                }
            }
            String json = objectMapper.writeValueAsString(snapshotValue);
            stringRedisTemplate.opsForValue().set(sessionKey, json, ttl);
            stringRedisTemplate.opsForValue().set(userSessionKey, sessionId, ttl);
            LoginUserContext context = toLoginUserContext(userId, username, sessionId, snapshotValue);
            return new SessionLoginResult(sessionId, context);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public java.util.Optional<SessionSnapshot> getSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return java.util.Optional.empty();
        }
        String json = stringRedisTemplate.opsForValue().get(buildSessionKey(sessionId));
        if (json == null || json.isBlank()) {
            return java.util.Optional.empty();
        }
        try {
            SessionSnapshotValue value = objectMapper.readValue(json, SessionSnapshotValue.class);
            LoginUserContext context = toLoginUserContext(value.userId(), value.username(), sessionId, value);
            return java.util.Optional.of(new SessionSnapshot(value.userId(), value.username(), value.nickname(), context));
        } catch (Exception ex) {
            stringRedisTemplate.delete(buildSessionKey(sessionId));
            return java.util.Optional.empty();
        }
    }

    @Override
    public void logout(String sessionId) {
        getSession(sessionId).ifPresent(snapshot -> {
            stringRedisTemplate.delete(buildSessionKey(sessionId));
            String userKey = buildUserSessionKey(snapshot.userId());
            String currentSid = stringRedisTemplate.opsForValue().get(userKey);
            if (sessionId.equals(currentSid)) {
                stringRedisTemplate.delete(userKey);
            }
        });
    }

    @Override
    public LoginUserContext buildLoginUserContext(Long userId, String username, String sessionId) {
        SessionSnapshot snapshot = getSession(sessionId).orElseThrow(() -> new BizException(ErrorCode.SESSION_INVALID));
        if (!snapshot.userId().equals(userId)) {
            throw new BizException(ErrorCode.SESSION_INVALID);
        }
        if (username != null && !username.isBlank() && !snapshot.username().equals(username)) {
            throw new BizException(ErrorCode.SESSION_INVALID);
        }
        return snapshot.context();
    }

    private SessionSnapshotValue buildSnapshot(Long userId, String username, String nickname) {
        List<String> roles = roleMapper.selectRoleCodesByUserId(userId);
        List<LyPermission> permissions = permissionMapper.selectPermissionsByUserId(userId);
        List<String> menus = new ArrayList<>();
        List<String> buttons = new ArrayList<>();
        List<String> apis = new ArrayList<>();
        for (LyPermission permission : permissions) {
            switch (permission.getPermType()) {
                case "MENU" -> menus.add(permission.getPermCode());
                case "BUTTON" -> buttons.add(permission.getPermCode());
                case "API" -> apis.add(permission.getPermCode());
                default -> {
                }
            }
        }
        return new SessionSnapshotValue(userId, username, nickname, roles, menus, buttons, apis);
    }

    private LoginUserContext toLoginUserContext(Long userId, String username, String sessionId, SessionSnapshotValue value) {
        return new LoginUserContext(userId, username, sessionId, value.roles(), value.menuPermissions(), value.buttonPermissions(), new HashSet<>(value.apiPermissions()));
    }

    private String buildSessionKey(String sessionId) {
        return ssoProperties.getSessionKeyPrefix() + sessionId;
    }

    private String buildUserSessionKey(Long userId) {
        return ssoProperties.getUserSessionKeyPrefix() + userId;
    }

    public record SessionSnapshotValue(Long userId,
                                       String username,
                                       String nickname,
                                       List<String> roles,
                                       List<String> menuPermissions,
                                       List<String> buttonPermissions,
                                       List<String> apiPermissions) {
    }
}
