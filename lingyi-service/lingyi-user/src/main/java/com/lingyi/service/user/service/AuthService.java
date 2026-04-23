package com.lingyi.service.user.service;

import com.lingyi.common.core.constant.GlobalConstants;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.user.dto.AuthLoginRequest;
import com.lingyi.service.user.dto.AuthLoginResponse;
import com.lingyi.service.user.dto.AuthRegisterRequest;
import com.lingyi.service.user.dto.CurrentUserResponse;
import com.lingyi.service.user.repository.UserAuthRepository;
import com.lingyi.service.user.repository.UserAuthRepository.LoginUserRow;
import com.lingyi.service.user.repository.UserAuthRepository.UserProfileRow;
import com.lingyi.service.user.util.JwtTokenService;
import com.lingyi.service.user.util.JwtTokenService.TokenData;
import com.lingyi.service.user.util.SnowflakeIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAuthRepository userAuthRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserAuthRepository userAuthRepository,
                       SnowflakeIdGenerator idGenerator,
                       JwtTokenService jwtTokenService) {
        this.userAuthRepository = userAuthRepository;
        this.idGenerator = idGenerator;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public AuthLoginResponse register(AuthRegisterRequest request) {
        if (userAuthRepository.existsUsername(request.getUsername())) {
            throw new BizException(ErrorCode.USER_ALREADY_EXISTS);
        }

        long userId = idGenerator.nextId();
        long authId = idGenerator.nextId();
        String nickname = request.getNickname() == null || request.getNickname().isBlank()
                ? request.getUsername()
                : request.getNickname();

        userAuthRepository.insertUser(userId, request.getUsername(), nickname);
        userAuthRepository.insertPasswordAuth(authId, userId, request.getUsername(), passwordEncoder.encode(request.getPassword()));

        TokenData tokenData = jwtTokenService.generateToken(userId, request.getUsername());
        return new AuthLoginResponse(userId, request.getUsername(), nickname, tokenData.token(), tokenData.expiresAt());
    }

    public AuthLoginResponse login(AuthLoginRequest request) {
        LoginUserRow user = userAuthRepository.findPasswordUser(request.getUsername())
                .orElseThrow(() -> new BizException(ErrorCode.USERNAME_OR_PASSWORD_INVALID));

        if (!passwordEncoder.matches(request.getPassword(), user.encryptedPassword())) {
            throw new BizException(ErrorCode.USERNAME_OR_PASSWORD_INVALID);
        }

        userAuthRepository.updateLastLoginAt(user.userId());
        TokenData tokenData = jwtTokenService.generateToken(user.userId(), user.username());
        return new AuthLoginResponse(user.userId(), user.username(), user.nickname(), tokenData.token(), tokenData.expiresAt());
    }

    public CurrentUserResponse currentUser(HttpServletRequest request) {
        Long userId = tryResolveUserId(request)
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED));

        UserProfileRow user = userAuthRepository.findUserById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED));

        return new CurrentUserResponse(user.userId(), user.username(), user.nickname());
    }

    private Optional<Long> tryResolveUserId(HttpServletRequest request) {
        String headerUserId = request.getHeader(GlobalConstants.USER_ID_HEADER);
        if (headerUserId != null && !headerUserId.isBlank()) {
            try {
                return Optional.of(Long.parseLong(headerUserId));
            } catch (NumberFormatException ignore) {
                return Optional.empty();
            }
        }

        String authorization = request.getHeader(GlobalConstants.AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(GlobalConstants.BEARER_PREFIX)) {
            return Optional.empty();
        }
        String token = authorization.substring(GlobalConstants.BEARER_PREFIX.length());
        return Optional.of(jwtTokenService.parseAndValidate(token).userId());
    }
}
