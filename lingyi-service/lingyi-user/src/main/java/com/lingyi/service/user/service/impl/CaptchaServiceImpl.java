package com.lingyi.service.user.service.impl;

import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.user.config.CaptchaProperties;
import com.lingyi.service.user.service.CaptchaService;
import com.lingyi.service.user.vo.CaptchaVO;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final String CAPTCHA_KEY_PREFIX = "lingyi:captcha:";
    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final StringRedisTemplate stringRedisTemplate;
    private final CaptchaProperties captchaProperties;

    public CaptchaServiceImpl(StringRedisTemplate stringRedisTemplate, CaptchaProperties captchaProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.captchaProperties = captchaProperties;
    }

    @Override
    public CaptchaVO generate() {
        String code = randomCode(captchaProperties.getLength());
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        Instant expireAt = Instant.now().plusSeconds(captchaProperties.getExpireSeconds());
        stringRedisTemplate.opsForValue().set(buildKey(captchaId), code, java.time.Duration.ofSeconds(captchaProperties.getExpireSeconds()));
        return new CaptchaVO(captchaId, drawImageBase64(code), expireAt);
    }

    @Override
    public void verifyAndConsume(String captchaId, String captchaCode) {
        String key = buildKey(captchaId);
        String expect = stringRedisTemplate.opsForValue().get(key);
        if (expect == null) {
            throw new BizException(ErrorCode.CAPTCHA_EXPIRED);
        }
        stringRedisTemplate.delete(key);
        if (captchaCode == null || !expect.equalsIgnoreCase(captchaCode.trim())) {
            throw new BizException(ErrorCode.CAPTCHA_INVALID);
        }
    }

    private String buildKey(String captchaId) {
        return CAPTCHA_KEY_PREFIX + captchaId;
    }

    private String randomCode(int len) {
        StringBuilder builder = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            builder.append(CHARSET.charAt((int) (Math.random() * CHARSET.length())));
        }
        return builder.toString();
    }

    private String drawImageBase64(String code) {
        try {
            String svg = buildSvg(code);
            return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new BizException(ErrorCode.CAPTCHA_GENERATE_FAILED);
        }
    }

    private String buildSvg(String code) {
        int width = captchaProperties.getWidth();
        int height = captchaProperties.getHeight();
        StringBuilder builder = new StringBuilder();
        builder.append("<svg xmlns='http://www.w3.org/2000/svg' width='")
                .append(width)
                .append("' height='")
                .append(height)
                .append("' viewBox='0 0 ")
                .append(width)
                .append(' ')
                .append(height)
                .append("'>")
                .append("<rect width='100%' height='100%' rx='10' fill='#f6fbfa'/>");

        for (int i = 0; i < 6; i++) {
            builder.append("<line x1='")
                    .append(rand(width))
                    .append("' y1='")
                    .append(rand(height))
                    .append("' x2='")
                    .append(rand(width))
                    .append("' y2='")
                    .append(rand(height))
                    .append("' stroke='#c8d9d5' stroke-width='1'/>");
        }

        for (int i = 0; i < code.length(); i++) {
            int x = 14 + i * 24;
            int y = 28 + rand(4);
            int rotate = rand(17) - 8;
            builder.append("<text x='")
                    .append(x)
                    .append("' y='")
                    .append(y)
                    .append("' font-size='24' font-weight='700' fill='#17302b' font-family='Arial, Microsoft YaHei, sans-serif' transform='rotate(")
                    .append(rotate)
                    .append(' ')
                    .append(x)
                    .append(' ')
                    .append(y)
                    .append(")'>")
                    .append(code.charAt(i))
                    .append("</text>");
        }

        builder.append("</svg>");
        return builder.toString();
    }

    private int rand(int bound) {
        return (int) (Math.random() * bound);
    }
}
