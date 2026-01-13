package com.mustapha.ecommerce.order.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.mustapha.ecommerce.order.domain.DomainEvent;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Order Paid Domain Event
 * 
 * Responsibility: Immutable record that an order was paid
 * 
 * Pattern: Domain Event (Java Record)
 * Layer: DOMAIN (not application!)
 * 
 * Domain Rules:
 * - orderId must not be null (every event must reference a valid order)
 * - paidAmount must not be null (event must record actual amount paid)
 * - occurredAt timestamp is required (when did payment happen?)
 * 
 * NOTE: This is a STRUCTURE-ONLY event per reviewer's advice.
 * DO NOT raise this event in Order.markAsPaid() yet - wait until we have a listener.
 * 
 * Potential Listeners (future):
 * - Inventory Service (reserve stock after payment)
 * - Email Service (send payment confirmation)
 * - Analytics Service (track revenue)
 * - Fulfillment Service (start processing order)
 */
public record OrderPaidEvent(
    String eventId,
    OrderId orderId,
    Money paidAmount,
    LocalDateTime occurredAt
) implements DomainEvent {
    
    /**
     * Compact constructor - validation for domain invariants
     */
    public OrderPaidEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null or blank");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null in OrderPaidEvent");
        }
        if (paidAmount == null) {
            throw new IllegalArgumentException("Paid amount cannot be null in OrderPaidEvent");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred at cannot be null in OrderPaidEvent");
        }
    }
    
    /**
     * Convenience constructor for creating new events (generates ID and timestamp)
     */
    public OrderPaidEvent(OrderId orderId, Money paidAmount) {
        this(UUID.randomUUID().toString(), orderId, paidAmount, LocalDateTime.now());
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
