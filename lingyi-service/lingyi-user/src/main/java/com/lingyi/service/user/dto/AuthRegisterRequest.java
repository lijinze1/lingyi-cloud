package com.lingyi.service.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 4, max = 32, message = "username length must be between 4 and 32")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 64, message = "password length must be between 6 and 64")
    private String password;

    @Size(max = 64, message = "nickname length must be less than 64")
    private String nickname;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
