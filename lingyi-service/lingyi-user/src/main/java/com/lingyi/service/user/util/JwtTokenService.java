package com.lingyi.service.user.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.common.web.domain.ErrorCode;
import com.lingyi.common.web.exception.BizException;
import com.lingyi.service.user.config.AuthProperties;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {

    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;

    public JwtTokenService(ObjectMapper objectMapper, AuthProperties authProperties) {
        this.objectMapper = objectMapper;
        this.authProperties = authProperties;
    }

    public TokenData generateToken(Long userId, String username, String sessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(authProperties.getTokenExpireSeconds());

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", authProperties.getIssuer());
        payload.put("sub", username);
        payload.put("uid", userId);
        payload.put("sid", sessionId);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String headerEncoded = encodeJson(header);
        String payloadEncoded = encodeJson(payload);
        String signInput = headerEncoded + "." + payloadEncoded;
        String signature = sign(signInput);

        return new TokenData(signInput + "." + signature, expiresAt);
    }

    public UserClaims parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BizException(ErrorCode.UNAUTHORIZED);
            }
            String signInput = parts[0] + "." + parts[1];
            String expected = sign(signInput);
            if (!constantTimeEquals(expected, parts[2])) {
                throw new BizException(ErrorCode.UNAUTHORIZED);
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<>() {
            });
            if (!authProperties.getIssuer().equals(String.valueOf(payload.get("iss")))) {
                throw new BizException(ErrorCode.UNAUTHORIZED);
            }
            long exp = Long.parseLong(String.valueOf(payload.get("exp")));
            if (Instant.now().getEpochSecond() >= exp) {
                throw new BizException(ErrorCode.UNAUTHORIZED);
            }
            return new UserClaims(
                    Long.parseLong(String.valueOf(payload.get("uid"))),
                    String.valueOf(payload.get("sub")),
                    String.valueOf(payload.get("sid"))
            );
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] left = a.getBytes(StandardCharsets.UTF_8);
        byte[] right = b.getBytes(StandardCharsets.UTF_8);
        if (left.length != right.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length; i++) {
            result |= left[i] ^ right[i];
        }
        return result == 0;
    }

    public record TokenData(String token, Instant expiresAt) {
    }

    public record UserClaims(Long userId, String username, String sessionId) {
    }
}
