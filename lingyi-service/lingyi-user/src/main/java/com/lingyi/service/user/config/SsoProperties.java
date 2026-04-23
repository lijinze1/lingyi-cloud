package com.lingyi.service.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lingyi.sso")
public class SsoProperties {

    private boolean singleSession = true;
    private String sessionKeyPrefix = "lingyi:sso:session:";
    private String userSessionKeyPrefix = "lingyi:sso:user:";

    public boolean isSingleSession() {
        return singleSession;
    }

    public void setSingleSession(boolean singleSession) {
        this.singleSession = singleSession;
    }

    public String getSessionKeyPrefix() {
        return sessionKeyPrefix;
    }

    public void setSessionKeyPrefix(String sessionKeyPrefix) {
        this.sessionKeyPrefix = sessionKeyPrefix;
    }

    public String getUserSessionKeyPrefix() {
        return userSessionKeyPrefix;
    }

    public void setUserSessionKeyPrefix(String userSessionKeyPrefix) {
        this.userSessionKeyPrefix = userSessionKeyPrefix;
    }
}
