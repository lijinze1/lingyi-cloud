package com.lingyi.service.user.service;

import com.lingyi.service.user.dto.AuthLoginRequest;
import com.lingyi.service.user.dto.AuthPasswordLoginRequest;
import com.lingyi.service.user.dto.AuthRegisterRequest;
import com.lingyi.service.user.dto.AuthSmsLoginRequest;
import com.lingyi.service.user.dto.AuthSmsRegisterRequest;
import com.lingyi.service.user.vo.AuthLoginVO;
import com.lingyi.service.user.vo.CurrentUserVO;
import com.lingyi.service.user.vo.SessionStatusVO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthLoginVO register(AuthRegisterRequest request);

    AuthLoginVO registerBySms(AuthSmsRegisterRequest request);

    AuthLoginVO login(AuthLoginRequest request);

    AuthLoginVO loginByPassword(AuthPasswordLoginRequest request);

    AuthLoginVO loginBySms(AuthSmsLoginRequest request);

    CurrentUserVO currentUser(HttpServletRequest request);

    void logout(HttpServletRequest request);

    SessionStatusVO sessionStatus(HttpServletRequest request);
}
