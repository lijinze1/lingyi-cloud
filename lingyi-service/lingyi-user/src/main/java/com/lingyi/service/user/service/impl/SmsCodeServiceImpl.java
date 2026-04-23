package com.lingyi.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.user.config.SmsProperties;
import com.lingyi.service.user.entity.LyUser;
import com.lingyi.service.user.mapper.UserMapper;
import com.lingyi.service.user.service.SmsCodeService;
import com.lingyi.service.user.vo.SmsCodeSendVO;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SmsCodeServiceImpl implements SmsCodeService {

    private static final String SMS_KEY_PREFIX = "lingyi:sms:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final SmsProperties smsProperties;
    private final UserMapper userMapper;

    public SmsCodeServiceImpl(StringRedisTemplate stringRedisTemplate,
                              ObjectMapper objectMapper,
                              SmsProperties smsProperties,
                              UserMapper userMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.smsProperties = smsProperties;
        this.userMapper = userMapper;
    }

    @Override
    public SmsCodeSendVO send(String phone, String scene) {
        String normalizedPhone = normalizePhone(phone);
        String normalizedScene = normalizeScene(scene);
        validateSceneEligibility(normalizedPhone, normalizedScene);

        String key = buildKey(normalizedScene, normalizedPhone);
        SmsRecord record = readRecord(key);
        Instant now = Instant.now();
        if (record != null && record.nextSendAt().isAfter(now)) {
            throw new BizException(ErrorCode.SMS_CODE_SEND_TOO_FREQUENT);
        }

        SmsRecord newRecord = new SmsRecord(
                smsProperties.getFixedCode(),
                now.plusSeconds(smsProperties.getExpireSeconds()),
                now.plusSeconds(smsProperties.getCooldownSeconds())
        );
        writeRecord(key, newRecord);
        return new SmsCodeSendVO(normalizedPhone, normalizedScene, smsProperties.getFixedCode(), smsProperties.getExpireSeconds(), smsProperties.getCooldownSeconds());
    }

    @Override
    public void verifyAndConsume(String phone, String scene, String smsCode) {
        String normalizedPhone = normalizePhone(phone);
        String normalizedScene = normalizeScene(scene);
        String key = buildKey(normalizedScene, normalizedPhone);
        SmsRecord record = readRecord(key);
        if (record == null) {
            throw new BizException(ErrorCode.SMS_CODE_EXPIRED);
        }
        if (record.expiresAt().isBefore(Instant.now())) {
            stringRedisTemplate.delete(key);
            throw new BizException(ErrorCode.SMS_CODE_EXPIRED);
        }
        if (smsCode == null || !record.code().equals(smsCode.trim())) {
            throw new BizException(ErrorCode.SMS_CODE_INVALID);
        }
        stringRedisTemplate.delete(key);
    }

    @Override
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
        Long count = userMapper.selectCount(new LambdaQueryWrapper<LyUser>()
                .eq(LyUser::getPhone, phone)
                .eq(LyUser::getIsDeleted, 0));
        boolean exists = count != null && count > 0;
        if ("register".equals(scene) && exists) {
            throw new BizException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        if ("login".equals(scene) && !exists) {
            throw new BizException(ErrorCode.PHONE_NOT_REGISTERED);
        }
    }

    private String buildKey(String scene, String phone) {
        return SMS_KEY_PREFIX + scene + ':' + phone;
    }

    private SmsRecord readRecord(String key) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, SmsRecord.class);
        } catch (Exception ex) {
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    private void writeRecord(String key, SmsRecord record) {
        try {
            String json = objectMapper.writeValueAsString(record);
            stringRedisTemplate.opsForValue().set(key, json, Duration.ofSeconds(smsProperties.getExpireSeconds()));
        } catch (Exception ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    public record SmsRecord(String code, Instant expiresAt, Instant nextSendAt) {
    }
}
