package com.lingyi.service.user.dto;

public class SmsCodeSendResponse {

    private String phone;
    private String scene;
    private String mockCode;
    private long expiresInSeconds;
    private long cooldownSeconds;

    public SmsCodeSendResponse() {
    }

    public SmsCodeSendResponse(String phone, String scene, String mockCode, long expiresInSeconds, long cooldownSeconds) {
        this.phone = phone;
        this.scene = scene;
        this.mockCode = mockCode;
        this.expiresInSeconds = expiresInSeconds;
        this.cooldownSeconds = cooldownSeconds;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getMockCode() { return mockCode; }
    public void setMockCode(String mockCode) { this.mockCode = mockCode; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
    public long getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(long cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }
}
