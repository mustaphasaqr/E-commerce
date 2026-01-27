package com.mustapha.ecommerce.user.auth.infrastructure.messaging;

import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;
import com.mustapha.ecommerce.user.domain.event.PasswordChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Password Changed Event Listener
 * Responsibility: React to PasswordChangedEvent by revoking all refresh tokens
 * Pattern: Event Listener (Consumer owns the listener)
 * 
 * Location: AUTH infrastructure (not User infrastructure)
 * Why: This listener deletes Auth aggregates (RefreshToken)
 * 
 * Flow:
 * 1. User changes password (User.changePassword() or User.resetPassword())
 * 2. User subdomain publishes PasswordChangedEvent
 * 3. This listener reacts
 * 4. Revokes ALL refresh tokens for the user
 * 
 * Security Requirement:
 * When a user changes their password, all existing authentication tokens should be invalidated
 * This prevents:
 * - Stolen tokens from continuing to work
 * - Compromised sessions from persisting
 * 
 * Note: LoginSessions remain valid (user doesn't get logged out immediately)
 * Only long-lived RefreshTokens are revoked
 */
@Component
public class PasswordChangedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PasswordChangedEventListener.class);
    private final RefreshTokenRepository refreshTokenRepository;

    public PasswordChangedEventListener(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @EventListener
    public void handle(PasswordChangedEvent event) {
        logger.info("🔑 Password changed event received: userId={}", event.userId());

        try {
            // Revoke all refresh tokens (security best practice)
            refreshTokenRepository.deleteAllByUserId(event.userId());
            
            logger.info("✅ Successfully revoked all refresh tokens after password change: {}", event.userId());
            
        } catch (Exception e) {
            // Log error but don't fail - password change is more important
            logger.error("❌ Failed to revoke refresh tokens after password change: {}", event.userId(), e);
        }
    }
}
