package com.lingyi.service.user.vo;

public record SmsCodeSendVO(String phone, String scene, String mockCode, long expiresInSeconds, long cooldownSeconds) {
}

