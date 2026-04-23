package com.lingyi.service.user.vo;

public record AdminPermissionVO(Long id, String permCode, String permName, String permType, String path, String method, Integer status, String remark) {
}

