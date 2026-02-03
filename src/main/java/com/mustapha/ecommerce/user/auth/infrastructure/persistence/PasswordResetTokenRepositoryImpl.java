package com.mustapha.ecommerce.user.auth.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.user.auth.domain.model.PasswordResetToken;
import com.mustapha.ecommerce.user.auth.domain.repository.PasswordResetTokenRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Password Reset Token Repository Implementation
 * Responsibility: Persist PasswordResetToken aggregate to Redis
 * Pattern: Adapter (Domain Repository → Redis)
 * 
 * Scope: AUTH subdomain only
 * Technology: Spring Data Redis
 * Storage: Redis (TTL: 24 hours)
 * 
 * Design:
 * - Key: password_reset:{tokenValue}
 * - Value: Serialized PasswordResetToken (JSON via RedisTemplate)
 * - TTL: 24 hours (auto-expire)
 * 
 * Security:
 * - Token is single-use (deleted after use)
 * - Auto-expires after 24 hours
 * - Cannot be reused after validation
 */
@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String KEY_PREFIX = "password_reset:";
    private static final long TTL_HOURS = 24;

    public PasswordResetTokenRepositoryImpl(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        String key = KEY_PREFIX + token.getToken();
        redisTemplate.opsForValue().set(key, token, TTL_HOURS, TimeUnit.HOURS);
        return token;
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String tokenValue) {
        String key = KEY_PREFIX + tokenValue;
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value).map(this::convertToPasswordResetToken);
    }

    private PasswordResetToken convertToPasswordResetToken(Object value) {
        if (value instanceof PasswordResetToken) {
            return (PasswordResetToken) value;
        }
        if (value instanceof LinkedHashMap) {
            LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) value;
            return PasswordResetToken.reconstitute(
                (String) map.get("token"),
                (String) map.get("userId"),
                (String) map.get("email"),
                parseLocalDateTime(map.get("createdAt")),
                parseLocalDateTime(map.get("expiresAt")),
                (Boolean) map.get("used"),
                map.get("usedAt") != null ? parseLocalDateTime(map.get("usedAt")) : null
            );
        }
        throw new IllegalStateException("Unexpected type for PasswordResetToken: " + value.getClass());
    }

    private LocalDateTime parseLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof String) return LocalDateTime.parse((String) value);
        if (value instanceof LinkedHashMap) {
            LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) value;
            return LocalDateTime.of(
                ((Number) map.get("year")).intValue(),
                ((Number) map.get("monthValue")).intValue(),
                ((Number) map.get("dayOfMonth")).intValue(),
                ((Number) map.get("hour")).intValue(),
                ((Number) map.get("minute")).intValue(),
                ((Number) map.get("second")).intValue(),
                ((Number) map.get("nano")).intValue()
            );
        }
        throw new IllegalStateException("Cannot parse LocalDateTime from: " + value.getClass());
    }

    @Override
    public void delete(String tokenValue) {
        String key = KEY_PREFIX + tokenValue;
        redisTemplate.delete(key);
    }

    @Override
    public void deleteAllByUserId(UserId userId) {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    PasswordResetToken token = convertToPasswordResetToken(value);
                    if (token.getUserId().equals(userId.getValue())) {
                        redisTemplate.delete(key);
                    }
                }
            }
        }
    }
}
