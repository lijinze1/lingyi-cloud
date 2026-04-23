package com.lingyi.service.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SmsCodeSendRequest {

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^1\\d{10}$", message = "phone format is invalid")
    private String phone;

    @NotBlank(message = "scene is required")
    private String scene;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
}
