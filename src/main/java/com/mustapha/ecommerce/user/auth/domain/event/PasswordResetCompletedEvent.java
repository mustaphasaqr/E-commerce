package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;

/**
 * Domain event raised when password is successfully reset.
 */
public record PasswordResetCompletedEvent(
        String userId,
        Instant occurredAt
) implements AuthDomainEvent {

    public PasswordResetCompletedEvent(String userId) {
        this(userId, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
