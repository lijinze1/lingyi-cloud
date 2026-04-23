package com.lingyi.service.user.controller;

import com.lingyi.common.web.domain.Result;
import com.lingyi.service.user.dto.AuthLoginRequest;
import com.lingyi.service.user.dto.AuthPasswordLoginRequest;
import com.lingyi.service.user.dto.AuthRegisterRequest;
import com.lingyi.service.user.dto.AuthSmsLoginRequest;
import com.lingyi.service.user.dto.AuthSmsRegisterRequest;
import com.lingyi.service.user.dto.SmsCodeSendRequest;
import com.lingyi.service.user.service.AuthService;
import com.lingyi.service.user.service.CaptchaService;
import com.lingyi.service.user.service.SmsCodeService;
import com.lingyi.service.user.vo.AuthLoginVO;
import com.lingyi.service.user.vo.CaptchaVO;
import com.lingyi.service.user.vo.CurrentUserVO;
import com.lingyi.service.user.vo.SessionStatusVO;
import com.lingyi.service.user.vo.SmsCodeSendVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/auth")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;
    private final SmsCodeService smsCodeService;

    public AuthController(AuthService authService, CaptchaService captchaService, SmsCodeService smsCodeService) {
        this.authService = authService;
        this.captchaService = captchaService;
        this.smsCodeService = smsCodeService;
    }

    @PostMapping("/register")
    public Result<AuthLoginVO> register(@Valid @RequestBody AuthRegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/register/sms")
    public Result<AuthLoginVO> registerBySms(@Valid @RequestBody AuthSmsRegisterRequest request) {
        return Result.success(authService.registerBySms(request));
    }

    @PostMapping("/login")
    public Result<AuthLoginVO> login(@Valid @RequestBody AuthLoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/login/password")
    public Result<AuthLoginVO> loginByPassword(@Valid @RequestBody AuthPasswordLoginRequest request) {
        return Result.success(authService.loginByPassword(request));
    }

    @PostMapping("/login/sms")
    public Result<AuthLoginVO> loginBySms(@Valid @RequestBody AuthSmsLoginRequest request) {
        return Result.success(authService.loginBySms(request));
    }

    @PostMapping("/sms-code/send")
    public Result<SmsCodeSendVO> sendSmsCode(@Valid @RequestBody SmsCodeSendRequest request) {
        return Result.success(smsCodeService.send(request.getPhone(), request.getScene()));
    }

    @GetMapping("/captcha")
    public Result<CaptchaVO> captcha() {
        return Result.success(captchaService.generate());
    }

    @GetMapping("/me")
    public Result<CurrentUserVO> currentUser(HttpServletRequest request) {
        return Result.success(authService.currentUser(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return Result.success((Void) null);
    }

    @GetMapping("/session/status")
    public Result<SessionStatusVO> sessionStatus(HttpServletRequest request) {
        return Result.success(authService.sessionStatus(request));
    }
}

