package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;

/**
 * Domain event raised when a user logs in successfully.
 */
public record UserLoggedInEvent(
        String userId,
        String sessionId,
        String ipAddress,
        String userAgent,
        Instant occurredAt
) implements AuthDomainEvent {

    public UserLoggedInEvent(String userId, String sessionId, String ipAddress, String userAgent) {
        this(userId, sessionId, ipAddress, userAgent, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
