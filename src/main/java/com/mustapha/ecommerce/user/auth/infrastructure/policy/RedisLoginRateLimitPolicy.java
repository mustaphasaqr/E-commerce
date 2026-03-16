package com.mustapha.ecommerce.user.auth.infrastructure.policy;

import com.mustapha.ecommerce.user.auth.domain.policy.LoginRateLimitPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based Login Rate Limit Policy (Production-Ready)
 * 
 * Advantages over in-memory:
 * - Works across multiple server instances (load balancer)
 * - Data persists across application restarts
 * - Centralized rate limiting for microservices
 * 
 * Redis Keys:
 * - rate_limit:user:{userId}:{timestamp} - Failed attempt marker
 * - rate_limit:ip:{ipAddress}:{timestamp} - IP attempt marker
 * 
 * TTL: Auto-expires after lockout window (30min for user, 60min for IP)
 */
@Component
public class RedisLoginRateLimitPolicy implements LoginRateLimitPolicy {

    private static final Logger logger = LoggerFactory.getLogger(RedisLoginRateLimitPolicy.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final boolean rateLimitingEnabled;
    private static final String USER_KEY_PREFIX = "rate_limit:user:";
    private static final String IP_KEY_PREFIX = "rate_limit:ip:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");

    public RedisLoginRateLimitPolicy(RedisTemplate<String, Object> redisTemplate, Environment environment) {
        this.redisTemplate = redisTemplate;
        this.rateLimitingEnabled = !environment.getProperty("rate-limiting.enabled", "true").equals("false");
    }

    @Override
    public RateLimitResult checkUserRateLimit(String userId) {
        if (!rateLimitingEnabled) {
            return RateLimitResult.allowed();
        }

        try {
            String keyPattern = USER_KEY_PREFIX + userId + ":*";
            Set<String> keys = redisTemplate.keys(keyPattern);

            if (keys == null || keys.isEmpty()) {
                return RateLimitResult.allowed();
            }

            // Count attempts in last 30 minutes
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES);
            long attemptCount = keys.stream()
                .map(this::extractTimestamp)
                .filter(timestamp -> timestamp != null && timestamp.isAfter(cutoff))
                .count();

            if (attemptCount >= MAX_ATTEMPTS_PER_USER) {
                LocalDateTime firstAttempt = keys.stream()
                    .map(this::extractTimestamp)
                    .filter(timestamp -> timestamp != null && timestamp.isAfter(cutoff))
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());

                LocalDateTime lockedUntil = firstAttempt.plusMinutes(LOCKOUT_DURATION_MINUTES);
                return RateLimitResult.denied(
                    "Too many failed login attempts. Try again after " + lockedUntil,
                    lockedUntil
                );
            }
        } catch (Exception e) {
            logger.warn("Redis unavailable during user rate-limit check for '{}'. Failing open. Cause: {}", userId, e.getMessage());
        }

        return RateLimitResult.allowed();
    }

    @Override
    public RateLimitResult checkIpRateLimit(String ipAddress) {
        if (!rateLimitingEnabled) {
            return RateLimitResult.allowed();
        }

        try {
            String keyPattern = IP_KEY_PREFIX + ipAddress + ":*";
            Set<String> keys = redisTemplate.keys(keyPattern);

            if (keys == null || keys.isEmpty()) {
                return RateLimitResult.allowed();
            }

            // Count attempts in last 60 minutes
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(IP_LOCKOUT_DURATION_MINUTES);
            long attemptCount = keys.stream()
                .map(this::extractTimestamp)
                .filter(timestamp -> timestamp != null && timestamp.isAfter(cutoff))
                .count();

            if (attemptCount >= MAX_ATTEMPTS_PER_IP) {
                LocalDateTime firstAttempt = keys.stream()
                    .map(this::extractTimestamp)
                    .filter(timestamp -> timestamp != null && timestamp.isAfter(cutoff))
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());

                LocalDateTime lockedUntil = firstAttempt.plusMinutes(IP_LOCKOUT_DURATION_MINUTES);
                return RateLimitResult.denied(
                    "Too many failed login attempts from this IP. Try again after " + lockedUntil,
                    lockedUntil
                );
            }
        } catch (Exception e) {
            logger.warn("Redis unavailable during IP rate-limit check for '{}'. Failing open. Cause: {}", ipAddress, e.getMessage());
        }

        return RateLimitResult.allowed();
    }

    @Override
    public void recordFailedAttempt(String userId, String ipAddress) {
        if (!rateLimitingEnabled) {
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(FORMATTER);

            // Record user attempt with TTL
            String userKey = USER_KEY_PREFIX + userId + ":" + timestamp;
            redisTemplate.opsForValue().set(userKey, "failed", LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);

            // Record IP attempt with TTL
            String ipKey = IP_KEY_PREFIX + ipAddress + ":" + timestamp;
            redisTemplate.opsForValue().set(ipKey, "failed", IP_LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            logger.warn("Redis unavailable while recording failed login attempt for user='{}' ip='{}'. Cause: {}",
                userId, ipAddress, e.getMessage());
        }
    }

    @Override
    public void recordSuccessfulLogin(String userId, String ipAddress) {
        if (!rateLimitingEnabled) {
            return;
        }

        try {
            // Clear user attempts (IP tracking remains for distributed attack detection)
            String keyPattern = USER_KEY_PREFIX + userId + ":*";
            Set<String> keys = redisTemplate.keys(keyPattern);

            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            logger.warn("Redis unavailable while clearing successful-login rate-limit keys for user='{}'. Cause: {}",
                userId, e.getMessage());
        }
    }

    /**
     * Extract timestamp from Redis key
     * Key format: rate_limit:user:{userId}:{timestamp}
     */
    private LocalDateTime extractTimestamp(String key) {
        try {
            String[] parts = key.split(":");
            if (parts.length >= 4) {
                String timestampStr = parts[3];
                return LocalDateTime.parse(timestampStr, FORMATTER);
            }
        } catch (Exception e) {
            // Invalid key format - ignore
        }
        return null;
    }
}
