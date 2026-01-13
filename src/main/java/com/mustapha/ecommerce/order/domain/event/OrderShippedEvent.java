package com.mustapha.ecommerce.order.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.mustapha.ecommerce.order.domain.DomainEvent;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Order Shipped Domain Event
 * 
 * Responsibility: Immutable record that an order was shipped
 * 
 * Pattern: Domain Event (Java Record)
 * Layer: DOMAIN (not application!)
 * 
 * Domain Rules:
 * - orderId must not be null (every event must reference a valid order)
 * - trackingNumber can be optional (some shipments don't have tracking)
 * - occurredAt timestamp is required (when did shipment happen?)
 * 
 * NOTE: This is a STRUCTURE-ONLY event per reviewer's advice.
 * DO NOT raise this event in Order.ship() yet - wait until we have a listener.
 * 
 * Potential Listeners (future):
 * - Email Service (send shipment notification)
 * - SMS Service (send tracking number to customer)
 * - Analytics Service (track fulfillment time)
 * - Customer Portal (update order status display)
 */
public record OrderShippedEvent(
    String eventId,
    OrderId orderId,
    String trackingNumber,  // Optional - can be null
    LocalDateTime occurredAt
) implements DomainEvent {
    
    /**
     * Compact constructor - validation for domain invariants
     */
    public OrderShippedEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null or blank");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null in OrderShippedEvent");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred at cannot be null in OrderShippedEvent");
        }
        // trackingNumber can be null - it's optional
    }
    
    /**
     * Convenience constructor for creating new events (generates ID and timestamp)
     */
    public OrderShippedEvent(OrderId orderId, String trackingNumber) {
        this(UUID.randomUUID().toString(), orderId, trackingNumber, LocalDateTime.now());
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
