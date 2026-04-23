package com.lingyi.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data

public class AdminPermissionSaveRequest {

    @NotBlank(message = "permCode is required")
    @Size(max = 128, message = "permCode length must be less than 128")
    private String permCode;

    @NotBlank(message = "permName is required")
    @Size(max = 128, message = "permName length must be less than 128")
    private String permName;

    @NotBlank(message = "permType is required")
    private String permType;

    private String path;
    private String method;
    private Integer status = 1;
    private String remark;

}

