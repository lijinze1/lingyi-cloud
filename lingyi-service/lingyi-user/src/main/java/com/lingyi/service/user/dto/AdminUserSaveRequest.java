package com.lingyi.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data

public class AdminUserSaveRequest {

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^1\\d{10}$", message = "phone format is invalid")
    private String phone;

    @Size(max = 64, message = "nickname length must be less than 64")
    private String nickname;

    private Integer status = 1;

}

