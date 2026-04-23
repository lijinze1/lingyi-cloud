package com.lingyi.service.user.convert;

import com.lingyi.service.user.entity.LyUser;
import com.lingyi.service.user.vo.AdminUserVO;
import java.util.List;

public final class UserConvert {

    private UserConvert() {
    }

    public static AdminUserVO toAdminUserVO(LyUser user, List<String> roles) {
        return new AdminUserVO(user.getId(), user.getUsername(), user.getNickname(), user.getPhone(), user.getStatus(), roles);
    }
}
