package com.lingyi.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data

public class AdminUserRoleBindRequest {

    @NotEmpty(message = "roleIds is required")
    private List<Long> roleIds;

}

