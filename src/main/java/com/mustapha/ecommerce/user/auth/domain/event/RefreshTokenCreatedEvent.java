package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Domain event raised when a refresh token is created.
 * Important for security audit and token lifecycle tracking.
 */
public record RefreshTokenCreatedEvent(
        String tokenValue,
        String userId,
        LocalDateTime expiresAt,
        Instant occurredAt
) implements AuthDomainEvent {

    public RefreshTokenCreatedEvent(String tokenValue, String userId, LocalDateTime expiresAt) {
        this(tokenValue, userId, expiresAt, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "RefreshTokenCreatedEvent{tokenValue='***', userId='" + userId + "', expiresAt=" + expiresAt + ", occurredAt=" + occurredAt + "}";
    }
}
