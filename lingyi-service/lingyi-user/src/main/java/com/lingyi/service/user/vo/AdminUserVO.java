package com.lingyi.service.user.vo;

import java.util.List;

public record AdminUserVO(
        Long userId,
        String username,
        String nickname,
        String phone,
        Integer status,
        List<String> roles
) {
}

