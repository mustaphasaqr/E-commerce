package com.mustapha.ecommerce.user.auth.infrastructure.persistence;

import com.mustapha.ecommerce.user.auth.domain.model.PasswordResetToken;
import com.mustapha.ecommerce.user.auth.domain.repository.PasswordResetTokenRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

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
    private static final String KEY_PREFIX = "password_reset:";
    private static final long TTL_HOURS = 24;

    public PasswordResetTokenRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
        return Optional.ofNullable(value).map(v -> (PasswordResetToken) v);
    }

    @Override
    public void delete(String tokenValue) {
        String key = KEY_PREFIX + tokenValue;
        redisTemplate.delete(key);
    }

    @Override
    public void deleteAllByUserId(UserId userId) {
        // Scan for all password_reset:* keys and filter by userId
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                Object value = redisTemplate.opsForValue().get(key);
                if (value instanceof PasswordResetToken) {
                    PasswordResetToken token = (PasswordResetToken) value;
                    if (token.getUserId().equals(userId.getValue())) {
                        redisTemplate.delete(key);
                    }
                }
            }
        }
    }
}
