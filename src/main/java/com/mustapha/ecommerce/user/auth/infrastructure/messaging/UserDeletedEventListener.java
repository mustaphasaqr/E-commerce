package com.mustapha.ecommerce.user.auth.infrastructure.messaging;

import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.auth.domain.repository.PasswordResetTokenRepository;
import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;
import com.mustapha.ecommerce.user.domain.event.UserDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * User Deleted Event Listener
 * Responsibility: React to UserDeletedEvent by cleaning up all Auth data
 * Pattern: Event Listener (Consumer owns the listener)
 * 
 * Location: AUTH infrastructure (not User infrastructure)
 * Why: This listener deletes Auth aggregates (RefreshToken, LoginSession, PasswordResetToken)
 * 
 * Flow:
 * 1. User subdomain publishes UserDeletedEvent
 * 2. This listener reacts
 * 3. Deletes ALL auth-related data for the user
 * 
 * GDPR Compliance:
 * When a user is deleted (right to be forgotten), ALL related data must be removed
 * This includes:
 * - RefreshTokens (30-day TTL)
 * - LoginSessions (24-hour TTL)
 * - PasswordResetTokens (24-hour TTL)
 */
@Component
public class UserDeletedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(UserDeletedEventListener.class);
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserDeletedEventListener(RefreshTokenRepository refreshTokenRepository,
                                   LoginSessionRepository loginSessionRepository,
                                   PasswordResetTokenRepository passwordResetTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginSessionRepository = loginSessionRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @EventListener
    public void handle(UserDeletedEvent event) {
        logger.info("🗑️ User deleted event received: userId={}, reason={}", 
            event.userId(), event.reason());

        try {
            // Delete all refresh tokens
            refreshTokenRepository.deleteAllByUserId(event.userId());
            logger.debug("✅ Deleted all refresh tokens for user: {}", event.userId());
            
            // Delete all login sessions
            loginSessionRepository.deleteAllByUserId(event.userId());
            logger.debug("✅ Deleted all login sessions for user: {}", event.userId());
            
            // Delete all password reset tokens
            passwordResetTokenRepository.deleteAllByUserId(event.userId());
            logger.debug("✅ Deleted all password reset tokens for user: {}", event.userId());
            
            logger.info("✅ Successfully cleaned up all Auth data for deleted user: {}", event.userId());
            
        } catch (Exception e) {
            // Log error but don't fail - user deletion is more important
            logger.error("❌ Failed to cleanup Auth data for deleted user: {}", event.userId(), e);
        }
    }
}
