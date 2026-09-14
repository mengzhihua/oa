package com.oa.system.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private final byte[] key;
    private final long tokenTtl;
    private final ObjectMapper objectMapper;

    public TokenService(@Value("${oa.auth.secret:}") String secret,
                        @Value("${oa.auth.token-ttl:12h}") Duration tokenTtl,
                        ObjectMapper objectMapper) {
        if (secret == null || secret.trim().isEmpty()) {
            this.key = new byte[32];
            new SecureRandom().nextBytes(this.key);
            log.warn("未配置 oa.auth.secret，当前启动使用随机令牌密钥；生产环境必须配置固定密钥");
        } else {
            this.key = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.tokenTtl = tokenTtl.toMillis();
        this.objectMapper = objectMapper;
    }

    public String issue(Long userId, String username, Set<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("typ", "oa");
        claims.put("exp", System.currentTimeMillis() + tokenTtl);
        return issue(claims);
    }

    public String issue(Long userId, String username) {
        return issue(userId, username, new HashSet<String>());
    }

    public String issueOauth(Long userId, String username, String clientId, long ttl) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("username", username);
        claims.put("cid", clientId);
        claims.put("typ", "oauth");
        claims.put("exp", System.currentTimeMillis() + ttl);
        return issue(claims);
    }

    public String issueOAuth(Long userId, String username, String clientId, long ttl) {
        return issueOauth(userId, username, clientId, ttl);
    }

    public String issue(Map<String, Object> claims) {
        try {
            String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}"
                    .getBytes(StandardCharsets.UTF_8));
            String payload = encode(objectMapper.writeValueAsBytes(claims));
            String body = header + "." + payload;
            return body + "." + encode(sign(body));
        } catch (Exception exception) {
            throw new IllegalStateException("令牌生成失败", exception);
        }
    }

    public Principal parse(String token) {
        try {
            if (token == null) {
                return null;
            }
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !MessageDigest.isEqual(
                    Base64.getUrlDecoder().decode(parts[2]), sign(parts[0] + "." + parts[1]))) {
                return null;
            }
            Map<String, Object> claims = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<Map<String, Object>>() {
                    });
            if (((Number) claims.get("exp")).longValue() < System.currentTimeMillis()) {
                return null;
            }
            Set<String> roles = new HashSet<>();
            Object roleClaim = claims.get("roles");
            if (roleClaim instanceof Iterable) {
                for (Object role : (Iterable<?>) roleClaim) {
                    roles.add(String.valueOf(role));
                }
            }
            Object subject = claims.get("sub");
            Long userId = subject instanceof Number ? ((Number) subject).longValue() : null;
            return new Principal(userId,
                    String.valueOf(claims.get("username")), String.valueOf(claims.get("typ")),
                    String.valueOf(claims.get("cid")), roles, claims);
        } catch (Exception exception) {
            return null;
        }
    }

    private byte[] sign(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("令牌签名失败", exception);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static class Principal {
        private final Long userId;
        private final String username;
        private final String type;
        private final String clientId;
        private final Set<String> roles;
        private final Map<String, Object> claims;

        public Principal(Long userId, String username, String type, String clientId,
                         Set<String> roles, Map<String, Object> claims) {
            this.userId = userId;
            this.username = username;
            this.type = type;
            this.clientId = clientId;
            this.roles = roles;
            this.claims = claims;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getType() {
            return type;
        }

        public String getClientId() {
            return clientId;
        }

        public Set<String> getRoles() {
            return roles;
        }

        public Map<String, Object> getClaims() {
            return claims;
        }
    }
}
