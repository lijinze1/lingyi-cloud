package com.lingyi.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data

public class AuthRegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 4, max = 32, message = "username length must be between 4 and 32")
    private String username;

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

