package com.lingyi.service.user.vo;

import java.time.Instant;

public record CaptchaVO(String captchaId, String imageBase64, Instant expireAt) {
}

