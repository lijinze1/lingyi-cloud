package com.lingyi.service.user.service;

import com.lingyi.service.user.security.LoginUserContext;
import java.util.Optional;

public interface AuthSessionService {

    SessionLoginResult createSession(Long userId, String username, String nickname);

    Optional<SessionSnapshot> getSession(String sessionId);

    void logout(String sessionId);

    LoginUserContext buildLoginUserContext(Long userId, String username, String sessionId);

    record SessionLoginResult(String sessionId, LoginUserContext context) {
    }

    record SessionSnapshot(Long userId, String username, String nickname, LoginUserContext context) {
    }
}
