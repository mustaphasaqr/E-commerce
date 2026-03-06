package com.mustapha.ecommerce.shared.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Token Cleanup Scheduled Job
 * 
 * Responsibility: Delete expired authentication tokens
 * - Password reset tokens (expired after 24 hours)
 * - Email verification tokens (expired after 7 days)
 * - Refresh tokens (expired after 30 days)
 * 
 * Schedule: Every day at 3:00 AM (low traffic time)
 * 
 * Why Critical:
 * - Prevents database bloat
 * - Security: Expired tokens cannot be used
 * - GDPR compliance: Don't keep unnecessary personal data
 * 
 * Pattern: Scheduled Background Job
 * Layer: INFRASTRUCTURE
 */
@Component
public class TokenCleanupJob {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupJob.class);
    
    // TODO: Inject repositories when token implementations are finalized
    // private final PasswordResetTokenRepository passwordResetTokenRepository;
    // private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    // private final RefreshTokenRepository refreshTokenRepository;
    
    /**
     * Run every day at 3:00 AM
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        logger.info("🧹 Starting expired token cleanup job...");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // TODO: Implement when token repositories have deleteExpired() methods
            // int deletedPasswordReset = passwordResetTokenRepository.deleteExpired();
            // int deletedEmailVerification = emailVerificationTokenRepository.deleteExpired();
            // int deletedRefresh = refreshTokenRepository.deleteExpired();
            
            int deletedPasswordReset = 0;
            int deletedEmailVerification = 0;
            int deletedRefresh = 0;
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            logger.info("✅ Token cleanup completed - Password Reset: {}, Email Verification: {}, Refresh: {} - Execution time: {}ms",
                deletedPasswordReset, deletedEmailVerification, deletedRefresh, executionTime);
                
        } catch (Exception e) {
            logger.error("❌ Token cleanup failed: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger error handler
        }
    }
    
    /**
     * Run every hour to clean up very old expired tokens
     * This catches tokens that may have been missed by daily cleanup
     */
    @Scheduled(cron = "0 0 * * * *")  // Every hour at minute 0
    @Transactional
    public void cleanupVeryOldTokens() {
        logger.debug("🧹 Running hourly cleanup for very old tokens (> 30 days)...");
        
        try {
            // TODO: Implement deleteVeryOldTokens (> 30 days) in repositories
            // This is a safety net for tokens that somehow survived daily cleanup
            
            logger.debug("✅ Hourly token cleanup completed");
        } catch (Exception e) {
            logger.error("❌ Hourly token cleanup failed: {}", e.getMessage(), e);
        }
    }
}
