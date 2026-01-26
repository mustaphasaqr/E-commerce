package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;

/**
 * Domain event raised when refresh token(s) are revoked.
 * Critical for security - mass token revocation on security breach, password change, or logout all devices.
 */
public record RefreshTokenRevokedEvent(
        String userId,
        String reason,
        int tokensRevoked,
        Instant occurredAt
) implements AuthDomainEvent {

    public RefreshTokenRevokedEvent(String userId, String reason, int tokensRevoked) {
        this(userId, reason, tokensRevoked, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
