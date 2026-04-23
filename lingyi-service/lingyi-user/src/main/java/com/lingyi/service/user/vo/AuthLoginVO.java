package com.lingyi.service.user.vo;

import java.time.Instant;
import java.util.List;

public record AuthLoginVO(
        Long userId,
        String username,
        String nickname,
        String token,
        Instant expiresAt,
        List<String> roles,
        PermissionSummaryVO permissions
) {
}

