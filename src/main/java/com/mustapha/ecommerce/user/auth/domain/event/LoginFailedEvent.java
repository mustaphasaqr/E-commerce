package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;

/**
 * Domain event raised when login fails.
 * Critical for security monitoring and alerting.
 */
public record LoginFailedEvent(
        String identifier,
        String reason,
        String ipAddress,
        int attemptNumber,
        Instant occurredAt
) implements AuthDomainEvent {

    public LoginFailedEvent(String identifier, String reason, String ipAddress, int attemptNumber) {
        this(identifier, reason, ipAddress, attemptNumber, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
