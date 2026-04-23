package com.lingyi.service.user.service;

import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.user.dto.CaptchaResponse;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    private static final long EXPIRE_SECONDS = 180L;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final Font FONT = new Font("Arial", Font.BOLD, 24);

    private final Map<String, CaptchaEntry> cache = new ConcurrentHashMap<>();

    public CaptchaResponse generate() {
        cleanupExpired();

        String code = randomCode(4);
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        Instant expireAt = Instant.now().plusSeconds(EXPIRE_SECONDS);
        cache.put(captchaId, new CaptchaEntry(code, expireAt));

        String imageBase64 = drawImageBase64(code);
        return new CaptchaResponse(captchaId, imageBase64, expireAt);
    }

    public void verifyAndConsume(String captchaId, String captchaCode) {
        cleanupExpired();
        CaptchaEntry entry = cache.remove(captchaId);
        if (entry == null || Instant.now().isAfter(entry.expireAt())) {
            throw new BizException("A0412", "Captcha expired");
        }
        if (!entry.code().equalsIgnoreCase(captchaCode)) {
            throw new BizException("A0411", "Captcha invalid");
        }
    }

    private String drawImageBase64(String code) {
        try {
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(new Color(245, 248, 255));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setFont(FONT);

            for (int i = 0; i < 8; i++) {
                g.setColor(new Color(rand(160), rand(160), rand(160)));
                g.drawLine(rand(WIDTH), rand(HEIGHT), rand(WIDTH), rand(HEIGHT));
            }
            for (int i = 0; i < code.length(); i++) {
                g.setColor(new Color(40 + rand(100), 40 + rand(100), 40 + rand(100)));
                g.drawString(String.valueOf(code.charAt(i)), 12 + i * 24, 30);
            }
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception ex) {
            throw new BizException("B0002", "Generate captcha failed");
        }
    }

    private String randomCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CHARSET.charAt(rand(CHARSET.length())));
        }
        return sb.toString();
    }

    private int rand(int bound) {
        return (int) (Math.random() * bound);
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        cache.entrySet().removeIf(e -> now.isAfter(e.getValue().expireAt()));
    }

    private record CaptchaEntry(String code, Instant expireAt) {
    }
}

