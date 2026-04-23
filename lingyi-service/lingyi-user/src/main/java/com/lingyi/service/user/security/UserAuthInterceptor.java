package com.lingyi.service.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.common.core.constant.GlobalConstants;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.domain.Result;
import com.lingyi.service.user.service.AuthSessionService;
import com.lingyi.service.user.util.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/user/ping",
            "/api/user/auth/login",
            "/api/user/auth/login/password",
            "/api/user/auth/login/sms",
            "/api/user/auth/register",
            "/api/user/auth/register/sms",
            "/api/user/auth/sms-code/send",
            "/api/user/auth/captcha",
            "/api/user/auth/session/status"
    );

    private final JwtTokenService jwtTokenService;
    private final AuthSessionService authSessionService;
    private final ObjectMapper objectMapper;

    public UserAuthInterceptor(JwtTokenService jwtTokenService, AuthSessionService authSessionService, ObjectMapper objectMapper) {
        this.jwtTokenService = jwtTokenService;
        this.authSessionService = authSessionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        LoginUserContext context = resolveContext(request);
        if (context == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, Result.fail(ErrorCode.UNAUTHORIZED));
            return false;
        }
        LoginUserContextHolder.set(context);

        RequirePermission requirePermission = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequirePermission.class);
        }
        if (requirePermission != null && !context.hasPermission(requirePermission.value())) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, Result.fail(ErrorCode.FORBIDDEN));
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContextHolder.clear();
    }

    private LoginUserContext resolveContext(HttpServletRequest request) {
        String userIdHeader = request.getHeader(GlobalConstants.USER_ID_HEADER);
        String usernameHeader = request.getHeader(GlobalConstants.USERNAME_HEADER);
        String sessionIdHeader = request.getHeader(GlobalConstants.SESSION_ID_HEADER);
        if (userIdHeader != null && sessionIdHeader != null) {
            try {
                return authSessionService.buildLoginUserContext(Long.parseLong(userIdHeader), usernameHeader, sessionIdHeader);
            } catch (Exception ignored) {
                return null;
            }
        }

        String authorization = request.getHeader(GlobalConstants.AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(GlobalConstants.BEARER_PREFIX)) {
            return null;
        }
        try {
            JwtTokenService.UserClaims claims = jwtTokenService.parseAndValidate(authorization.substring(GlobalConstants.BEARER_PREFIX.length()));
            return authSessionService.buildLoginUserContext(claims.userId(), claims.username(), claims.sessionId());
        } catch (Exception ignored) {
            return null;
        }
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}