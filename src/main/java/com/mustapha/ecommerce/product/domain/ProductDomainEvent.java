package com.mustapha.ecommerce.product.domain;

import java.time.LocalDateTime;

/**
 * Product Domain Event Marker Interface
 * 
 * Base interface for all domain events in the Product bounded context.
 * 
 * Purpose:
 * - Type-safe marker for product domain events
 * - Enforce common contract (eventId, timestamp)
 * - Enable polymorphic event handling
 * 
 * Pattern: Domain Event + Marker Interface
 * 
 * Contract Requirements:
 * - All domain events MUST be immutable (final class, final fields)
 * - All domain events MUST have a unique event ID
 * - All domain events MUST have a timestamp (when did it occur?)
 * 
 * Usage Example:
 * <pre>
 * public final class ProductCreatedEvent implements ProductDomainEvent {
 *     private final String eventId;
 *     private final LocalDateTime occurredAt;
 *     // ... other fields
 * }
 * </pre>
 */
public interface ProductDomainEvent {
    
    /**
     * Get the unique identifier for this event
     * 
     * @return Unique event ID (typically UUID)
     */
    String getEventId();
    
    /**
     * Get the timestamp when this event occurred
     * 
     * @return LocalDateTime when the event happened
     */
    LocalDateTime getOccurredAt();
}
