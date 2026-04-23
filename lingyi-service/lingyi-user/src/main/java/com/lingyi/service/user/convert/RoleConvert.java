package com.lingyi.service.user.convert;

import com.lingyi.service.user.entity.LyRole;
import com.lingyi.service.user.vo.AdminRoleVO;

public final class RoleConvert {

    private RoleConvert() {
    }

    public static AdminRoleVO toAdminRoleVO(LyRole role) {
        return new AdminRoleVO(role.getId(), role.getRoleCode(), role.getRoleName(), role.getStatus(), role.getRemark());
    }
}
