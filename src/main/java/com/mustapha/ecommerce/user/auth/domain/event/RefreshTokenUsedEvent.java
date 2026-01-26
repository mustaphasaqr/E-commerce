package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;

/**
 * Domain event raised when refresh token is used.
 */
public record RefreshTokenUsedEvent(
        String tokenValue,
        String userId,
        String newSessionId,
        Instant occurredAt
) implements AuthDomainEvent {

    public RefreshTokenUsedEvent(String tokenValue, String userId, String newSessionId) {
        this(tokenValue, userId, newSessionId, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "RefreshTokenUsedEvent{tokenValue='***', userId='" + userId + "', newSessionId='" + newSessionId + "', occurredAt=" + occurredAt + "}";
    }
}
