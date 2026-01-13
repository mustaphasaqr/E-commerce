package com.mustapha.ecommerce.order.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.mustapha.ecommerce.order.domain.DomainEvent;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Order Delivered Domain Event
 * 
 * Responsibility: Immutable record that an order was delivered
 * 
 * Pattern: Domain Event (Java Record)
 * Layer: DOMAIN (not application!)
 * 
 * Domain Rules:
 * - orderId must not be null (every event must reference a valid order)
 * - occurredAt timestamp is required (when did delivery happen?)
 * 
 * NOTE: This is a STRUCTURE-ONLY event per reviewer's advice.
 * DO NOT raise this event in Order.deliver() yet - wait until we have a listener.
 * 
 * Potential Listeners (future):
 * - Email Service (send delivery confirmation)
 * - Review Service (trigger review request after X days)
 * - Analytics Service (track delivery time metrics)
 * - Customer Portal (update order status to complete)
 * - Loyalty Program (award points for completed purchase)
 */
public record OrderDeliveredEvent(
    String eventId,
    OrderId orderId,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    /**
     * Compact constructor - validation for domain invariants
     */
    public OrderDeliveredEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null or blank");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null in OrderDeliveredEvent");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred at cannot be null in OrderDeliveredEvent");
        }
    }
    
    /**
     * Convenience constructor for creating new events (generates ID and timestamp)
     */
    public OrderDeliveredEvent(OrderId orderId) {
        this(UUID.randomUUID().toString(), orderId, LocalDateTime.now());
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
