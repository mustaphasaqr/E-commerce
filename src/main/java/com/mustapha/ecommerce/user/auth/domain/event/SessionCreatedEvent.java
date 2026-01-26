package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Domain event raised when a login session is created.
 * Important for session management and concurrent session tracking.
 */
public record SessionCreatedEvent(
        String sessionId,
        String userId,
        String ipAddress,
        String userAgent,
        LocalDateTime expiresAt,
        Instant occurredAt
) implements AuthDomainEvent {

    public SessionCreatedEvent(String sessionId, String userId, String ipAddress, String userAgent, LocalDateTime expiresAt) {
        this(sessionId, userId, ipAddress, userAgent, expiresAt, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
