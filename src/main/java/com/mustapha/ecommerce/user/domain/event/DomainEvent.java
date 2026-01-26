package com.mustapha.ecommerce.user.domain.event;

import java.time.Instant;

/**
 * Marker interface for all User domain events.
 * Provides type safety and cleaner event publishing.
 */
public interface DomainEvent {
    /**
     * When this event occurred.
     */
    Instant getOccurredAt();
}
