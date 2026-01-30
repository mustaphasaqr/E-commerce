package com.mustapha.ecommerce.user.auth.infrastructure.policy;

import com.mustapha.ecommerce.user.auth.domain.policy.LoginRateLimitPolicy;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Login Rate Limit Policy (Development/Testing Only)
 * 
 * WARNING: This implementation is NOT suitable for production with multiple servers!
 * - Data lost on application restart
 * - Does not work with load balancers (each server has separate memory)
 * - No coordination across microservices
 * 
 * Use RedisLoginRateLimitPolicy for production.
 * 
 * To activate this instead of Redis, change @Component to @Component
 * and remove @Component from RedisLoginRateLimitPolicy.
 */
// @Component  // ← Commented out - using Redis instead
public class InMemoryLoginRateLimitPolicy implements LoginRateLimitPolicy {

    private final Map<String, AttemptRecord> userAttempts = new ConcurrentHashMap<>();
    private final Map<String, AttemptRecord> ipAttempts = new ConcurrentHashMap<>();

    @Override
    public RateLimitResult checkUserRateLimit(String userId) {
        LocalDateTime now = LocalDateTime.now();
        AttemptRecord record = userAttempts.get(userId);
        
        if (record == null) {
            return RateLimitResult.allowed();
        }
        
        record.cleanOldAttempts(now, LOCKOUT_DURATION_MINUTES);
        
        if (record.getCount() >= MAX_ATTEMPTS_PER_USER) {
            LocalDateTime lockedUntil = record.getFirstAttempt().plusMinutes(LOCKOUT_DURATION_MINUTES);
            return RateLimitResult.denied(
                "Too many failed login attempts. Try again after " + lockedUntil,
                lockedUntil
            );
        }
        
        return RateLimitResult.allowed();
    }

    @Override
    public RateLimitResult checkIpRateLimit(String ipAddress) {
        LocalDateTime now = LocalDateTime.now();
        AttemptRecord record = ipAttempts.get(ipAddress);
        
        if (record == null) {
            return RateLimitResult.allowed();
        }
        
        record.cleanOldAttempts(now, IP_LOCKOUT_DURATION_MINUTES);
        
        if (record.getCount() >= MAX_ATTEMPTS_PER_IP) {
            LocalDateTime lockedUntil = record.getFirstAttempt().plusMinutes(IP_LOCKOUT_DURATION_MINUTES);
            return RateLimitResult.denied(
                "Too many failed login attempts from this IP. Try again after " + lockedUntil,
                lockedUntil
            );
        }
        
        return RateLimitResult.allowed();
    }

    @Override
    public void recordFailedAttempt(String userId, String ipAddress) {
        LocalDateTime now = LocalDateTime.now();
        userAttempts.computeIfAbsent(userId, k -> new AttemptRecord()).addAttempt(now);
        ipAttempts.computeIfAbsent(ipAddress, k -> new AttemptRecord()).addAttempt(now);
    }

    @Override
    public void recordSuccessfulLogin(String userId, String ipAddress) {
        userAttempts.remove(userId);
        // IP tracking NOT cleared (per domain rule - prevent distributed attacks)
    }

    private static class AttemptRecord {
        private final Map<LocalDateTime, Boolean> attempts = new ConcurrentHashMap<>();

        public void addAttempt(LocalDateTime timestamp) {
            attempts.put(timestamp, true);
        }

        public void cleanOldAttempts(LocalDateTime now, int windowMinutes) {
            LocalDateTime cutoff = now.minusMinutes(windowMinutes);
            attempts.keySet().removeIf(timestamp -> timestamp.isBefore(cutoff));
        }

        public int getCount() {
            return attempts.size();
        }

        public LocalDateTime getFirstAttempt() {
            return attempts.keySet().stream()
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        }
    }
}
