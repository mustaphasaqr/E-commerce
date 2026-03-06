package com.mustapha.ecommerce.order.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.mustapha.ecommerce.order.domain.DomainEvent;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Order Cancelled Domain Event
 * 
 * Responsibility: Immutable record that an order was cancelled
 * 
 * Pattern: Domain Event (Java Record)
 * Layer: DOMAIN (not application!)
 * 
 * Domain Rules:
 * - orderId must not be null (every event must reference a valid order)
 * - items must not be null/empty (need to release stock)
 * - reason can be optional (sometimes we don't know why)
 * - occurredAt timestamp is required (when did cancellation happen?)
 * 
 * Listeners:
 * - Product Service (release reserved stock for all items)
 * - Payment Service (process refund if already paid)
 * - Email Service (send cancellation notification)
 * - Analytics Service (track cancellation rate)
 */
public record OrderCancelledEvent(
    String eventId,
    OrderId orderId,
    List<OrderItemDto> items,  // Products to release stock for
    String reason,  // Optional - can be null
    LocalDateTime occurredAt
) implements DomainEvent {
    
    /**
     * Compact constructor - validation for domain invariants
     */
    public OrderCancelledEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null or blank");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null in OrderCancelledEvent");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be null or empty in OrderCancelledEvent");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred at cannot be null in OrderCancelledEvent");
        }
        // reason can be null - it's optional
    }
    
    /**
     * Convenience constructor for creating new events (generates ID and timestamp)
     */
    public OrderCancelledEvent(OrderId orderId, List<OrderItemDto> items, String reason) {
        this(UUID.randomUUID().toString(), orderId, items, reason, LocalDateTime.now());
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
