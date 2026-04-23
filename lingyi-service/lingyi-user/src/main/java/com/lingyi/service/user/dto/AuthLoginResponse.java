package com.lingyi.service.user.dto;

import java.time.Instant;

public class AuthLoginResponse {

    private Long userId;
    private String username;
    private String nickname;
    private String token;
    private Instant expiresAt;

    public AuthLoginResponse() {
    }

    public AuthLoginResponse(Long userId, String username, String nickname, String token, Instant expiresAt) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
