package com.mustapha.ecommerce.user.auth.domain.policy;

import com.mustapha.ecommerce.user.auth.domain.exception.RateLimitExceededException;

import java.time.LocalDateTime;

/**
 * Login Rate Limit Policy (Auth Subdomain - Domain Rules Only)
 * 
 * Defines BUSINESS RULES for brute-force protection:
 * - Max 5 failed attempts per user within 30 minutes
 * - Max 20 failed attempts per IP within 1 hour  
 * - Automatic unlock after lockout period
 * 
 * CRITICAL ARCHITECTURE:
 * - This interface defines WHAT the rules are (domain layer)
 * - Infrastructure implements HOW to enforce them (Redis, in-memory, etc.)
 * - Domain should NEVER contain storage implementation (ConcurrentHashMap, Redis)
 * 
 * Implementation will be in Infrastructure layer:
 * - RedisLoginRateLimitPolicy (production - distributed)
 * - InMemoryLoginRateLimitPolicy (testing/dev - single instance)
 */
public interface LoginRateLimitPolicy {
    
    /**
     * Business rule constants (domain-defined)
     */
    int MAX_ATTEMPTS_PER_USER = 5;
    int LOCKOUT_DURATION_MINUTES = 30;
    int MAX_ATTEMPTS_PER_IP = 20;
    int IP_LOCKOUT_DURATION_MINUTES = 60;
    
    /**
     * Checks if login is allowed for user based on failed attempt tracking
     */
    RateLimitResult checkUserRateLimit(String userId);
    
    /**
     * Checks if login is allowed from IP address
     */
    RateLimitResult checkIpRateLimit(String ipAddress);
    
    /**
     * Records failed login attempt for both user and IP
     */
    void recordFailedAttempt(String userId, String ipAddress);
    
    /**
     * Clears user's failed attempt tracking on successful login
     * Note: IP tracking NOT cleared to prevent distributed attacks
     */
    void recordSuccessfulLogin(String userId, String ipAddress);
    
    /**
     * Result of rate limit check (domain value object)
     */
    class RateLimitResult {
        private final boolean allowed;
        private final String reason;
        private final LocalDateTime lockedUntil;

        private RateLimitResult(boolean allowed, String reason, LocalDateTime lockedUntil) {
            this.allowed = allowed;
            this.reason = reason;
            this.lockedUntil = lockedUntil;
        }

        public static RateLimitResult allowed() {
            return new RateLimitResult(true, null, null);
        }

        public static RateLimitResult denied(String reason, LocalDateTime lockedUntil) {
            return new RateLimitResult(false, reason, lockedUntil);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }

        public LocalDateTime getLockedUntil() {
            return lockedUntil;
        }

        public void throwIfDenied() {
            if (!allowed) {
                throw new RateLimitExceededException(reason, lockedUntil);
            }
        }
    }
}
