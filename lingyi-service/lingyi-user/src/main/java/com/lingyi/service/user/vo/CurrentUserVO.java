package com.lingyi.service.user.vo;

import java.util.List;

public record CurrentUserVO(
        Long userId,
        String username,
        String nickname,
        List<String> roles,
        PermissionSummaryVO permissions
) {
}

