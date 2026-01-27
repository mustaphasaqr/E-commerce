package com.mustapha.ecommerce.user.auth.infrastructure.messaging;

import com.mustapha.ecommerce.user.auth.application.command.LogoutAllDevicesCommand;
import com.mustapha.ecommerce.user.auth.application.usecase.LogoutAllDevicesUseCase;
import com.mustapha.ecommerce.user.domain.event.UserBlockedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * User Blocked Event Listener
 * Responsibility: React to UserBlockedEvent by revoking all sessions
 * Pattern: Event Listener (Consumer owns the listener)
 * 
 * Location: AUTH infrastructure (not User infrastructure)
 * Why: This listener calls Auth use cases, acts on Auth aggregates
 * 
 * Flow:
 * 1. User subdomain publishes UserBlockedEvent (via DomainEventPublisher)
 * 2. This listener reacts (Spring @EventListener)
 * 3. Calls LogoutAllDevicesUseCase to revoke sessions
 * 4. Auth aggregates (RefreshToken, LoginSession) deleted
 * 
 * Security Requirement:
 * Blocked users must NOT be able to continue using the app with existing tokens
 */
@Component
public class UserBlockedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(UserBlockedEventListener.class);
    private final LogoutAllDevicesUseCase logoutAllDevicesUseCase;

    public UserBlockedEventListener(LogoutAllDevicesUseCase logoutAllDevicesUseCase) {
        this.logoutAllDevicesUseCase = logoutAllDevicesUseCase;
    }

    @EventListener
    public void handle(UserBlockedEvent event) {
        logger.info("🚫 User blocked event received: userId={}, reason={}", 
            event.userId(), event.reason());

        try {
            // Revoke ALL sessions and refresh tokens (no exception for current session)
            LogoutAllDevicesCommand command = new LogoutAllDevicesCommand(
                event.userId(),
                null // Revoke ALL sessions including current
            );
            
            logoutAllDevicesUseCase.execute(command);
            
            logger.info("✅ Successfully revoked all sessions for blocked user: {}", event.userId());
            
        } catch (Exception e) {
            // Log error but don't fail - blocking user is more important than session cleanup
            logger.error("❌ Failed to revoke sessions for blocked user: {}", event.userId(), e);
        }
    }
}
