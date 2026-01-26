package com.mustapha.ecommerce.user.auth.domain.event;

import java.time.Instant;

/**
 * Marker interface for all Auth domain events.
 */
public interface AuthDomainEvent {
    /**
     * When this event occurred.
     */
    Instant getOccurredAt();
}
