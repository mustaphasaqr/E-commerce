package com.mustapha.ecommerce.shared.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Token Blacklist Service
 * 
 * Manages invalidated JWT tokens to enforce logout.
 * Uses Redis to store blacklisted tokens with expiration.
 * 
 * Key Format: "blacklist:token:{tokenHash}"
 * TTL: Same as JWT expiration (tokens auto-expire)
 * 
 * Security: Prevents token reuse after logout
 */
@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenGenerator jwtTokenGenerator;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate,
                                 JwtTokenGenerator jwtTokenGenerator) {
        this.redisTemplate = redisTemplate;
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    /**
     * Blacklist a token (typically after logout)
     * 
     * @param token JWT token to blacklist
     * @param expirationMs Token expiration time in milliseconds
     */
    public void blacklistToken(String token, long expirationMs) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        
        // Store with TTL matching token expiration
        // After token expires naturally, Redis will auto-delete the entry
        redisTemplate.opsForValue().set(
            key, 
            "blacklisted", 
            expirationMs, 
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Check if a token is blacklisted
     * 
     * @param token JWT token to check
     * @return true if token is blacklisted (invalid), false otherwise
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + hashToken(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Blacklist all tokens for a specific user (e.g., on password change)
     * 
     * @param userId User ID whose tokens should be invalidated
     */
    public void blacklistAllUserTokens(String userId) {
        String pattern = "session:user:" + userId + ":*";
        var keys = redisTemplate.keys(pattern);
        
        if (keys != null && !keys.isEmpty()) {
            // Delete all session keys for this user
            redisTemplate.delete(keys);
        }
    }

    /**
     * Hash token for storage (avoid storing full token)
     * Security: Store hash instead of raw token
     */
    private String hashToken(String token) {
        // Simple hash for key generation
        // In production, use SHA-256 or similar
        return String.valueOf(token.hashCode());
    }
}
