package com.lingyi.service.user.dto;

import java.time.Instant;

public class CaptchaResponse {

    private String captchaId;
    private String imageBase64;
    private Instant expireAt;

    public CaptchaResponse() {
    }

    public CaptchaResponse(String captchaId, String imageBase64, Instant expireAt) {
        this.captchaId = captchaId;
        this.imageBase64 = imageBase64;
        this.expireAt = expireAt;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public Instant getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Instant expireAt) {
        this.expireAt = expireAt;
    }
}

