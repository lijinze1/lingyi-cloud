package com.lingyi.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lingyi.common.core.constant.GlobalConstants;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.user.dto.AuthLoginRequest;
import com.lingyi.service.user.dto.AuthPasswordLoginRequest;
import com.lingyi.service.user.dto.AuthRegisterRequest;
import com.lingyi.service.user.dto.AuthSmsLoginRequest;
import com.lingyi.service.user.dto.AuthSmsRegisterRequest;
import com.lingyi.service.user.entity.LyUser;
import com.lingyi.service.user.entity.LyUserAuth;
import com.lingyi.service.user.enums.AuthTypeEnum;
import com.lingyi.service.user.service.AuthService;
import com.lingyi.service.user.service.AuthSessionService;
import com.lingyi.service.user.service.CaptchaService;
import com.lingyi.service.user.service.SmsCodeService;
import com.lingyi.service.user.mapper.UserAuthMapper;
import com.lingyi.service.user.mapper.UserMapper;
import com.lingyi.service.user.security.LoginUserContext;
import com.lingyi.service.user.util.JwtTokenService;
import com.lingyi.service.user.util.JwtTokenService.TokenData;
import com.lingyi.service.user.util.JwtTokenService.UserClaims;
import com.lingyi.service.user.util.SnowflakeIdGenerator;
import com.lingyi.service.user.vo.AuthLoginVO;
import com.lingyi.service.user.vo.CurrentUserVO;
import com.lingyi.service.user.vo.PermissionSummaryVO;
import com.lingyi.service.user.vo.SessionStatusVO;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final JwtTokenService jwtTokenService;
    private final CaptchaService captchaService;
    private final SmsCodeService smsCodeService;
    private final AuthSessionService authSessionService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserMapper userMapper,
                           UserAuthMapper userAuthMapper,
                           SnowflakeIdGenerator idGenerator,
                           JwtTokenService jwtTokenService,
                           CaptchaService captchaService,
                           SmsCodeService smsCodeService,
                           AuthSessionService authSessionService) {
        this.userMapper = userMapper;
        this.userAuthMapper = userAuthMapper;
        this.idGenerator = idGenerator;
        this.jwtTokenService = jwtTokenService;
        this.captchaService = captchaService;
        this.smsCodeService = smsCodeService;
        this.authSessionService = authSessionService;
    }

    @Override
    @Transactional
    public AuthLoginVO register(AuthRegisterRequest request) {
        captchaService.verifyAndConsume(request.getCaptchaId(), request.getCaptchaCode());
        String username = request.getUsername().trim();
        if (existsUserByUsername(username)) {
            throw new BizException(ErrorCode.USER_ALREADY_EXISTS);
        }
        LyUser user = createUser(username, request.getNickname(), null);
        createOrUpdatePasswordAuth(user.getId(), username, request.getPassword());
        return issueLogin(user);
    }

    @Override
    @Transactional
    public AuthLoginVO registerBySms(AuthSmsRegisterRequest request) {
        captchaService.verifyAndConsume(request.getCaptchaId(), request.getCaptchaCode());
        String phone = smsCodeService.normalizePhone(request.getPhone());
        smsCodeService.verifyAndConsume(phone, "register", request.getSmsCode());
        if (existsUserByPhone(phone)) {
            throw new BizException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        String nickname = request.getNickname() == null || request.getNickname().isBlank() ? "用户" + phone.substring(phone.length() - 4) : request.getNickname().trim();
        LyUser user = createUser(phone, nickname, phone);
        createOrUpdatePasswordAuth(user.getId(), phone, request.getPassword());
        return issueLogin(user);
    }

    @Override
    public AuthLoginVO login(AuthLoginRequest request) {
        captchaService.verifyAndConsume(request.getCaptchaId(), request.getCaptchaCode());
        LyUser user = loadUserByPasswordAccount(request.getUsername().trim(), request.getPassword());
        return issueLogin(user);
    }

    @Override
    public AuthLoginVO loginByPassword(AuthPasswordLoginRequest request) {
        captchaService.verifyAndConsume(request.getCaptchaId(), request.getCaptchaCode());
        LyUser user = loadUserByPasswordAccount(smsCodeService.normalizePhone(request.getPhone()), request.getPassword());
        return issueLogin(user);
    }

    @Override
    public AuthLoginVO loginBySms(AuthSmsLoginRequest request) {
        captchaService.verifyAndConsume(request.getCaptchaId(), request.getCaptchaCode());
        String phone = smsCodeService.normalizePhone(request.getPhone());
        smsCodeService.verifyAndConsume(phone, "login", request.getSmsCode());
        LyUser user = userMapper.selectOne(new LambdaQueryWrapper<LyUser>()
                .eq(LyUser::getPhone, phone)
                .eq(LyUser::getIsDeleted, 0)
                .last("LIMIT 1"));
        validateUserAvailable(user);
        return issueLogin(user);
    }

    @Override
    public CurrentUserVO currentUser(HttpServletRequest request) {
        UserClaims claims = resolveClaims(request).orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED));
        AuthSessionService.SessionSnapshot snapshot = authSessionService.getSession(claims.sessionId())
                .orElseThrow(() -> new BizException(ErrorCode.SESSION_INVALID));
        LyUser user = userMapper.selectById(snapshot.userId());
        validateUserAvailable(user);
        return new CurrentUserVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                snapshot.context().getRoles(),
                new PermissionSummaryVO(snapshot.context().getMenuPermissions(), snapshot.context().getButtonPermissions(), List.copyOf(snapshot.context().getApiPermissions()))
        );
    }

    @Override
    public void logout(HttpServletRequest request) {
        UserClaims claims = resolveClaims(request).orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED));
        authSessionService.logout(claims.sessionId());
    }

    @Override
    public SessionStatusVO sessionStatus(HttpServletRequest request) {
        Optional<UserClaims> claims = resolveClaims(request);
        if (claims.isEmpty()) {
            return new SessionStatusVO(false, null, null);
        }
        return authSessionService.getSession(claims.get().sessionId())
                .map(session -> new SessionStatusVO(true, session.userId(), session.username()))
                .orElseGet(() -> new SessionStatusVO(false, null, null));
    }

    private LyUser createUser(String username, String nickname, String phone) {
        LyUser user = new LyUser();
        user.setId(idGenerator.nextId());
        user.setUsername(username);
        user.setNickname((nickname == null || nickname.isBlank()) ? username : nickname.trim());
        user.setPhone(phone);
        user.setStatus(1);
        user.setIsDeleted(0);
        userMapper.insert(user);
        return user;
    }

    private void createOrUpdatePasswordAuth(Long userId, String authKey, String rawPassword) {
        LyUserAuth auth = userAuthMapper.selectOne(new LambdaQueryWrapper<LyUserAuth>()
                .eq(LyUserAuth::getUserId, userId)
                .eq(LyUserAuth::getAuthType, AuthTypeEnum.PASSWORD.name())
                .eq(LyUserAuth::getAuthKey, authKey)
                .eq(LyUserAuth::getIsDeleted, 0)
                .last("LIMIT 1"));
        String encryptedPassword = passwordEncoder.encode(rawPassword);
        if (auth == null) {
            auth = new LyUserAuth();
            auth.setId(idGenerator.nextId());
            auth.setUserId(userId);
            auth.setAuthType(AuthTypeEnum.PASSWORD.name());
            auth.setAuthKey(authKey);
            auth.setAuthSecret(encryptedPassword);
            auth.setStatus(1);
            auth.setIsDeleted(0);
            userAuthMapper.insert(auth);
            return;
        }
        auth.setAuthSecret(encryptedPassword);
        auth.setStatus(1);
        userAuthMapper.updateById(auth);
    }

    private LyUser loadUserByPasswordAccount(String authKey, String rawPassword) {
        LyUserAuth auth = userAuthMapper.selectOne(new LambdaQueryWrapper<LyUserAuth>()
                .eq(LyUserAuth::getAuthType, AuthTypeEnum.PASSWORD.name())
                .eq(LyUserAuth::getAuthKey, authKey)
                .eq(LyUserAuth::getStatus, 1)
                .eq(LyUserAuth::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (auth == null) {
            throw new BizException(ErrorCode.USERNAME_OR_PASSWORD_INVALID);
        }
        LyUser user = userMapper.selectById(auth.getUserId());
        validateUserAvailable(user);
        if (!passwordEncoder.matches(rawPassword, auth.getAuthSecret())) {
            throw new BizException(ErrorCode.USERNAME_OR_PASSWORD_INVALID);
        }
        return user;
    }

    private AuthLoginVO issueLogin(LyUser user) {
        userMapper.update(null, new LambdaUpdateWrapper<LyUser>()
                .eq(LyUser::getId, user.getId())
                .set(LyUser::getLastLoginAt, LocalDateTime.now()));
        AuthSessionService.SessionLoginResult sessionResult = authSessionService.createSession(user.getId(), user.getUsername(), user.getNickname());
        TokenData tokenData = jwtTokenService.generateToken(user.getId(), user.getUsername(), sessionResult.sessionId());
        LoginUserContext context = sessionResult.context();
        return new AuthLoginVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                tokenData.token(),
                tokenData.expiresAt(),
                context.getRoles(),
                new PermissionSummaryVO(context.getMenuPermissions(), context.getButtonPermissions(), List.copyOf(context.getApiPermissions()))
        );
    }

    private Optional<UserClaims> resolveClaims(HttpServletRequest request) {
        String headerUserId = request.getHeader(GlobalConstants.USER_ID_HEADER);
        String headerUsername = request.getHeader(GlobalConstants.USERNAME_HEADER);
        String headerSessionId = request.getHeader(GlobalConstants.SESSION_ID_HEADER);
        if (headerUserId != null && !headerUserId.isBlank() && headerSessionId != null && !headerSessionId.isBlank()) {
            try {
                return Optional.of(new UserClaims(Long.parseLong(headerUserId), headerUsername, headerSessionId));
            } catch (NumberFormatException ignore) {
                return Optional.empty();
            }
        }
        String authorization = request.getHeader(GlobalConstants.AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(GlobalConstants.BEARER_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(jwtTokenService.parseAndValidate(authorization.substring(GlobalConstants.BEARER_PREFIX.length())));
    }

    private void validateUserAvailable(LyUser user) {
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BizException(ErrorCode.USER_DISABLED);
        }
    }

    private boolean existsUserByUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<LyUser>()
                .eq(LyUser::getUsername, username)
                .eq(LyUser::getIsDeleted, 0)) > 0;
    }

    private boolean existsUserByPhone(String phone) {
        return userMapper.selectCount(new LambdaQueryWrapper<LyUser>()
                .eq(LyUser::getPhone, phone)
                .eq(LyUser::getIsDeleted, 0)) > 0;
    }
}
