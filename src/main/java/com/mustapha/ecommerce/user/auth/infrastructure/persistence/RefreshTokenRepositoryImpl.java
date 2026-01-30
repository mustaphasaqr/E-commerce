package com.mustapha.ecommerce.user.auth.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.user.auth.domain.model.RefreshToken;
import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Refresh Token Repository Implementation
 * Responsibility: Persist RefreshToken aggregate to Redis
 * Pattern: Adapter (Domain Repository → Redis)
 * 
 * Scope: AUTH subdomain only
 * Technology: Spring Data Redis
 * Storage: Redis (TTL: 30 days)
 * 
 * Design:
 * - Key: refresh_token:{tokenValue}
 * - Value: Serialized RefreshToken (JSON via RedisTemplate)
 * - TTL: 30 days (auto-expire)
 */
@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String KEY_PREFIX = "refresh_token:";
    private static final long TTL_DAYS = 30;

    public RefreshTokenRepositoryImpl(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        String key = KEY_PREFIX + token.getTokenValue();
        redisTemplate.opsForValue().set(key, token, TTL_DAYS, TimeUnit.DAYS);
        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(String tokenValue) {
        String key = KEY_PREFIX + tokenValue;
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return Optional.empty();
        }
        
        // Handle case where Redis returns LinkedHashMap instead of RefreshToken
        if (value instanceof LinkedHashMap) {
            try {
                RefreshToken token = objectMapper.convertValue(value, RefreshToken.class);
                return Optional.of(token);
            } catch (IllegalArgumentException e) {
                // Log and return empty if conversion fails
                return Optional.empty();
            }
        }
        
        // Direct cast if already RefreshToken
        return Optional.of((RefreshToken) value);
    }

    @Override
    public void delete(String tokenValue) {
        String key = KEY_PREFIX + tokenValue;
        redisTemplate.delete(key);
    }

    @Override
    public void deleteAllByUserId(UserId userId) {
        // Scan for all refresh_token:* keys and filter by userId
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                Object value = redisTemplate.opsForValue().get(key);
                if (value instanceof RefreshToken) {
                    RefreshToken token = (RefreshToken) value;
                    if (token.getUserId().equals(userId.getValue())) {
                        redisTemplate.delete(key);
                    }
                }
            }
        }
    }
}
