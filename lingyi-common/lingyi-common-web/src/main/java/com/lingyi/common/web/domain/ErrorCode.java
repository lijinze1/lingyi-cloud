package com.lingyi.common.web.domain;

public enum ErrorCode {
    SUCCESS("00000", "成功"),
    BAD_REQUEST("A0400", "请求参数错误"),
    UNAUTHORIZED("A0401", "登录状态已过期，请重新登录"),
    FORBIDDEN("A0403", "暂无访问权限"),
    USER_ALREADY_EXISTS("A0409", "账号已存在"),
    USERNAME_OR_PASSWORD_INVALID("A0410", "账号或密码错误"),
    CAPTCHA_INVALID("A0411", "图形验证码错误"),
    CAPTCHA_EXPIRED("A0412", "图形验证码已过期，请重新获取"),
    PHONE_FORMAT_INVALID("A0413", "请输入正确的 11 位手机号"),
    SMS_CODE_INVALID("A0414", "短信验证码错误"),
    SMS_CODE_EXPIRED("A0415", "请先获取验证码"),
    SMS_CODE_SEND_TOO_FREQUENT("A0416", "验证码发送过于频繁，请稍后再试"),
    SMS_SCENE_INVALID("A0417", "验证码场景不正确"),
    PHONE_ALREADY_EXISTS("A0418", "该手机号已注册"),
    PHONE_NOT_REGISTERED("A0419", "该手机号尚未注册"),
    USER_DISABLED("A0420", "当前账号已被禁用"),
    ROLE_CODE_ALREADY_EXISTS("A0421", "角色编码已存在"),
    PERMISSION_CODE_ALREADY_EXISTS("A0422", "权限编码已存在"),
    SESSION_INVALID("A0423", "登录状态已失效，请重新登录"),
    RESOURCE_NOT_FOUND("A0424", "目标数据不存在"),
    INTERNAL_ERROR("B0001", "服务器内部错误"),
    CAPTCHA_GENERATE_FAILED("B0002", "验证码生成失败");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
