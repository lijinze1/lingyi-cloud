package com.lingyi.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data

public class AdminUserPasswordResetRequest {

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 64, message = "password length must be between 6 and 64")
    private String password;

}

