package com.lingyi.service.user.security;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LoginUserContext {

    private final Long userId;
    private final String username;
    private final String sessionId;
    private final List<String> roles;
    private final List<String> menuPermissions;
    private final List<String> buttonPermissions;
    private final Set<String> apiPermissions;

    public LoginUserContext(Long userId,
                            String username,
                            String sessionId,
                            List<String> roles,
                            List<String> menuPermissions,
                            List<String> buttonPermissions,
                            Set<String> apiPermissions) {
        this.userId = userId;
        this.username = username;
        this.sessionId = sessionId;
        this.roles = roles == null ? Collections.emptyList() : roles;
        this.menuPermissions = menuPermissions == null ? Collections.emptyList() : menuPermissions;
        this.buttonPermissions = buttonPermissions == null ? Collections.emptyList() : buttonPermissions;
        this.apiPermissions = apiPermissions == null ? Collections.emptySet() : apiPermissions;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getMenuPermissions() {
        return menuPermissions;
    }

    public List<String> getButtonPermissions() {
        return buttonPermissions;
    }

    public Set<String> getApiPermissions() {
        return apiPermissions;
    }

    public boolean hasPermission(String permissionCode) {
        return apiPermissions.contains(permissionCode);
    }
}
