package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;

/**
 * Domain event raised when password reset token is created.
 * Used to trigger email notification.
 */
public record PasswordResetTokenCreatedEvent(
        String token,
        String userId,
        String email,
        Instant occurredAt
) implements AuthDomainEvent {

    public PasswordResetTokenCreatedEvent(String token, String userId, String email) {
        this(token, userId, email, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "PasswordResetTokenCreatedEvent{token='***', userId='" + userId + "', email='" + email + "', occurredAt=" + occurredAt + "}";
    }
}
