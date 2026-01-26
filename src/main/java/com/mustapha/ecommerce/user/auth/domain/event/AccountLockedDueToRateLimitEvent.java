package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Domain event raised when user account is locked due to rate limiting.
 */
public record AccountLockedDueToRateLimitEvent(
        String userId,
        String ipAddress,
        int failedAttempts,
        LocalDateTime lockedUntil,
        Instant occurredAt
) implements AuthDomainEvent {

    public AccountLockedDueToRateLimitEvent(String userId, String ipAddress, int failedAttempts, LocalDateTime lockedUntil) {
        this(userId, ipAddress, failedAttempts, lockedUntil, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
