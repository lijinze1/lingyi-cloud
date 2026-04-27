package com.lingyi.gateway.server.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingyi.gateway.server.config.AuthProperties;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
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

    public UserClaims parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String signInput = parts[0] + "." + parts[1];
            String expected = sign(signInput);
            if (!constantTimeEquals(expected, parts[2])) {
                return null;
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<>() {
            });

            long exp = Long.parseLong(String.valueOf(payload.get("exp")));
            if (Instant.now().getEpochSecond() >= exp) {
                return null;
            }
            if (!authProperties.getIssuer().equals(String.valueOf(payload.get("iss")))) {
                return null;
            }

            Long userId = Long.parseLong(String.valueOf(payload.get("uid")));
            String username = String.valueOf(payload.get("sub"));
            String sessionId = String.valueOf(payload.get("sid"));
            if (sessionId == null || sessionId.isBlank() || "null".equalsIgnoreCase(sessionId)) {
                return null;
            }
            return new UserClaims(userId, username, sessionId);
        } catch (Exception ex) {
            return null;
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(key);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
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

    public record UserClaims(Long userId, String username, String sessionId) {
    }
}