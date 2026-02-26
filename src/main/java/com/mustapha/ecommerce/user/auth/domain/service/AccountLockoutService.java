package com.mustapha.ecommerce.user.auth.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Account Lockout Service
 * 
 * Protects against brute force attacks by temporarily locking accounts
 * after multiple failed login attempts.
 * 
 * Security Policy:
 * - Max 5 failed login attempts per account
 * - 15-minute lockout period after 5 failures
 * - Counter resets on successful login
 * - Separate tracking per user account (not IP-based)
 * 
 * Why Account-Level (Not IP-Level):
 * - IP-based: Blocks legitimate users behind shared NAT/proxy
 * - Account-level: More precise, prevents specific account takeover
 * - We already have IP rate limiting in GlobalApiRateLimitFilter
 * 
 * Redis Keys:
 * - "account_lockout:failed_attempts:{userId}" → Failed attempt count
 * - "account_lockout:locked:{userId}" → Lock flag with TTL
 * 
 * Comparison with ExponentialBackoffFilter:
 * - ExponentialBackoff: Delays requests per IP (network-level defense)
 * - AccountLockout: Blocks account access (account-level defense)
 * - Both work together (defense in depth)
 */
@Service
public class AccountLockoutService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountLockoutService.class);
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);
    private static final Duration ATTEMPT_WINDOW = Duration.ofHours(1); // Reset attempts after 1 hour of inactivity
    
    private static final String FAILED_ATTEMPTS_KEY_PREFIX = "account_lockout:failed_attempts:";
    private static final String LOCKED_KEY_PREFIX = "account_lockout:locked:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public AccountLockoutService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Checks if account is currently locked.
     * 
     * @param userId User identifier (email, username, or user ID)
     * @return true if account is locked
     */
    public boolean isAccountLocked(String userId) {
        String lockedKey = LOCKED_KEY_PREFIX + userId;
        Boolean locked = redisTemplate.hasKey(lockedKey);
        return locked != null && locked;
    }
    
    /**
     * Records a failed login attempt.
     * If max attempts exceeded, locks the account.
     * 
     * @param userId User identifier
     * @return true if account was locked due to this attempt
     */
    public boolean recordFailedAttempt(String userId) {
        String attemptsKey = FAILED_ATTEMPTS_KEY_PREFIX + userId;
        
        // Increment failed attempts
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        
        if (attempts == null) {
            attempts = 1L;
        }
        
        // Set expiry on first attempt (sliding window)
        if (attempts == 1) {
            redisTemplate.expire(attemptsKey, ATTEMPT_WINDOW.toMillis(), TimeUnit.MILLISECONDS);
        }
        
        logger.warn("Failed login attempt #{} for user: {}", attempts, maskUserId(userId));
        
        // Lock account if max attempts exceeded
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(userId);
            logger.warn("🔒 Account LOCKED for user: {} (too many failed attempts)", maskUserId(userId));
            return true;
        }
        
        return false;
    }
    
    /**
     * Locks an account for the configured duration.
     * 
     * @param userId User identifier
     */
    private void lockAccount(String userId) {
        String lockedKey = LOCKED_KEY_PREFIX + userId;
        
        // Set lock flag with TTL
        redisTemplate.opsForValue().set(
            lockedKey, 
            "locked", 
            LOCKOUT_DURATION.toMillis(), 
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Resets failed login attempts (called on successful login).
     * 
     * @param userId User identifier
     */
    public void resetFailedAttempts(String userId) {
        String attemptsKey = FAILED_ATTEMPTS_KEY_PREFIX + userId;
        redisTemplate.delete(attemptsKey);
        
        logger.debug("Reset failed login attempts for user: {}", maskUserId(userId));
    }
    
    /**
     * Manually unlocks an account (admin action).
     * Use case: Customer support unlocking legitimate user.
     * 
     * @param userId User identifier
     */
    public void unlockAccount(String userId) {
        String lockedKey = LOCKED_KEY_PREFIX + userId;
        String attemptsKey = FAILED_ATTEMPTS_KEY_PREFIX + userId;
        
        redisTemplate.delete(lockedKey);
        redisTemplate.delete(attemptsKey);
        
        logger.info("✅ Account manually unlocked for user: {}", maskUserId(userId));
    }
    
    /**
     * Gets failed attempt count for a user.
     * Useful for monitoring and alerting.
     * 
     * @param userId User identifier
     * @return Number of failed attempts, or 0 if none
     */
    public int getFailedAttemptCount(String userId) {
        String attemptsKey = FAILED_ATTEMPTS_KEY_PREFIX + userId;
        Object attempts = redisTemplate.opsForValue().get(attemptsKey);
        
        if (attempts instanceof Integer) {
            return (Integer) attempts;
        } else if (attempts instanceof Long) {
            return ((Long) attempts).intValue();
        }
        
        return 0;
    }
    
    /**
     * Gets remaining lockout time in seconds.
     * 
     * @param userId User identifier
     * @return Seconds until unlock, or 0 if not locked
     */
    public long getRemainingLockoutSeconds(String userId) {
        if (!isAccountLocked(userId)) {
            return 0;
        }
        
        String lockedKey = LOCKED_KEY_PREFIX + userId;
        Long ttl = redisTemplate.getExpire(lockedKey, TimeUnit.SECONDS);
        
        return ttl != null && ttl > 0 ? ttl : 0;
    }
    
    /**
     * Throws exception if account is locked.
     * Call this before processing login.
     * 
     * @param userId User identifier
     * @throws AccountLockedException if account is locked
     */
    public void checkAccountNotLocked(String userId) {
        if (isAccountLocked(userId)) {
            long remainingSeconds = getRemainingLockoutSeconds(userId);
            long remainingMinutes = (remainingSeconds + 59) / 60; // Round up
            
            throw new AccountLockedException(
                String.format(
                    "Account is temporarily locked due to multiple failed login attempts. " +
                    "Please try again in %d minute(s).",
                    remainingMinutes
                ),
                remainingSeconds
            );
        }
    }
    
    /**
     * Masks user ID for logging (privacy).
     * Example: "user@example.com" → "u***@example.com"
     */
    private String maskUserId(String userId) {
        if (userId == null || userId.length() <= 4) {
            return "***";
        }
        
        if (userId.contains("@")) {
            // Email: show first char + domain
            int atIndex = userId.indexOf('@');
            return userId.charAt(0) + "***@" + userId.substring(atIndex + 1);
        }
        
        // Other: show first 2 chars
        return userId.substring(0, 2) + "***";
    }
}
