package com.lingyi.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data

public class AuthSmsRegisterRequest {

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^1\\d{10}$", message = "phone format is invalid")
    private String phone;

    @NotBlank(message = "smsCode is required")
    private String smsCode;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 64, message = "password length must be between 6 and 64")
    private String password;

    @Size(max = 64, message = "nickname length must be less than 64")
    private String nickname;

    @NotBlank(message = "captchaId is required")
    private String captchaId;

    @NotBlank(message = "captchaCode is required")
    private String captchaCode;

}

