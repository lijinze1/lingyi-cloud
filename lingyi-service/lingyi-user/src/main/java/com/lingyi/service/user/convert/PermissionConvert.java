package com.lingyi.service.user.convert;

import com.lingyi.service.user.entity.LyPermission;
import com.lingyi.service.user.vo.AdminPermissionVO;

public final class PermissionConvert {

    private PermissionConvert() {
    }

    public static AdminPermissionVO toAdminPermissionVO(LyPermission permission) {
        return new AdminPermissionVO(
                permission.getId(),
                permission.getPermCode(),
                permission.getPermName(),
                permission.getPermType(),
                permission.getPath(),
                permission.getMethod(),
                permission.getStatus(),
                permission.getRemark()
        );
    }
}
