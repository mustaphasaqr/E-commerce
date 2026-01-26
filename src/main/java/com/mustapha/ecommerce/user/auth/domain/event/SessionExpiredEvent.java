package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Domain event raised when a session expires passively (time-based, not user logout).
 * Important for cleanup jobs and session analytics (passive vs active session termination).
 */
public record SessionExpiredEvent(
        String sessionId,
        String userId,
        LocalDateTime expiredAt,
        Instant occurredAt
) implements AuthDomainEvent {

    public SessionExpiredEvent(String sessionId, String userId, LocalDateTime expiredAt) {
        this(sessionId, userId, expiredAt, Instant.now());
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
