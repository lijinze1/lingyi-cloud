package com.lingyi.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data

public class AdminRolePermissionBindRequest {

    @NotEmpty(message = "permissionIds is required")
    private List<Long> permissionIds;

}

