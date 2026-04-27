package com.lingyi.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data

public class AuthSmsLoginRequest {

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^1\\d{10}$", message = "phone format is invalid")
    private String phone;

    @NotBlank(message = "smsCode is required")
    private String smsCode;

    @NotBlank(message = "captchaId is required")
    private String captchaId;

    @NotBlank(message = "captchaCode is required")
    private String captchaCode;

}

