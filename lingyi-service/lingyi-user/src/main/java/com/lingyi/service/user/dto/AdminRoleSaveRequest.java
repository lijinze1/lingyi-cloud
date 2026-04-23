package com.lingyi.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data

public class AdminRoleSaveRequest {

    @NotBlank(message = "roleCode is required")
    @Size(max = 64, message = "roleCode length must be less than 64")
    private String roleCode;

    @NotBlank(message = "roleName is required")
    @Size(max = 64, message = "roleName length must be less than 64")
    private String roleName;

    private Integer status = 1;
    private String remark;

}

