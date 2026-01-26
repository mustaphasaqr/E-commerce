package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;

/**
 * Domain event raised when a user logs out.
 */
public record UserLoggedOutEvent(
        String userId,
        String sessionId,
        Instant occurredAt
) implements AuthDomainEvent {

    public UserLoggedOutEvent(String userId, String sessionId) {
        this(userId, sessionId, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
