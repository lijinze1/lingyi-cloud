package com.lingyi.service.user.vo;

import java.util.List;

public record PermissionSummaryVO(
        List<String> menus,
        List<String> buttons,
        List<String> apis
) {
}

