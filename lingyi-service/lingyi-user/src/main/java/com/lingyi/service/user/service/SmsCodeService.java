package com.lingyi.service.user.service;

import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.user.dto.SmsCodeSendResponse;
import com.lingyi.service.user.repository.UserAuthRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SmsCodeService {

    private static final String FIXED_CODE = "123456";
    private static final long EXPIRE_SECONDS = 300;
    private static final long COOLDOWN_SECONDS = 60;

    private final Map<String, SmsCodeRecord> cache = new ConcurrentHashMap<>();
    private final UserAuthRepository userAuthRepository;

    public SmsCodeService(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    public SmsCodeSendResponse send(String phone, String scene) {
        String normalizedPhone = normalizePhone(phone);
        String normalizedScene = normalizeScene(scene);
        validateSceneEligibility(normalizedPhone, normalizedScene);

        String key = buildKey(normalizedScene, normalizedPhone);
        SmsCodeRecord existing = cache.get(key);
        Instant now = Instant.now();
        if (existing != null && existing.nextSendAt().isAfter(now)) {
            throw new BizException(ErrorCode.SMS_CODE_SEND_TOO_FREQUENT);
        }

        cache.put(key, new SmsCodeRecord(
                FIXED_CODE,
                now.plusSeconds(EXPIRE_SECONDS),
                now.plusSeconds(COOLDOWN_SECONDS)
        ));
        return new SmsCodeSendResponse(normalizedPhone, normalizedScene, FIXED_CODE, EXPIRE_SECONDS, COOLDOWN_SECONDS);
    }

    public void verifyAndConsume(String phone, String scene, String smsCode) {
        String normalizedPhone = normalizePhone(phone);
        String normalizedScene = normalizeScene(scene);
        String normalizedCode = smsCode == null ? "" : smsCode.trim();

        String key = buildKey(normalizedScene, normalizedPhone);
        SmsCodeRecord record = cache.get(key);
        if (record == null) {
            throw new BizException(ErrorCode.SMS_CODE_EXPIRED);
        }
        if (record.expiresAt().isBefore(Instant.now())) {
            cache.remove(key);
            throw new BizException(ErrorCode.SMS_CODE_EXPIRED);
        }
        if (!record.code().equals(normalizedCode)) {
            throw new BizException(ErrorCode.SMS_CODE_INVALID);
        }
        cache.remove(key);
    }

    public String normalizePhone(String phone) {
        String normalized = phone == null ? "" : phone.trim();
        if (!normalized.matches("^1\\d{10}$")) {
            throw new BizException(ErrorCode.PHONE_FORMAT_INVALID);
        }
        return normalized;
    }

    private String normalizeScene(String scene) {
        String normalized = scene == null ? "" : scene.trim().toLowerCase(Locale.ROOT);
        if (!"register".equals(normalized) && !"login".equals(normalized)) {
            throw new BizException(ErrorCode.SMS_SCENE_INVALID);
        }
        return normalized;
    }

    private void validateSceneEligibility(String phone, String scene) {
        boolean exists = userAuthRepository.existsPhone(phone);
        if ("register".equals(scene) && exists) {
            throw new BizException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        if ("login".equals(scene) && !exists) {
            throw new BizException(ErrorCode.PHONE_NOT_REGISTERED);
        }
    }

    private String buildKey(String scene, String phone) {
        return scene + ":" + phone;
    }

    private record SmsCodeRecord(String code, Instant expiresAt, Instant nextSendAt) {
    }
}
