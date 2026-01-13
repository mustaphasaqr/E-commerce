package com.mustapha.ecommerce.order.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.mustapha.ecommerce.order.domain.DomainEvent;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Order Placed Domain Event
 * Responsibility: Immutable record that an order was placed
 * Pattern: Domain Event (Java Record)
 * Layer: DOMAIN (not application!)
 * 
 * Domain Rules:
 * - orderId must not be null (every event must reference a valid order)
 * - customerId must not be null (must know who placed the order)
 * - totalAmount must not be null (event must record actual amount)
 * - occurredAt timestamp is required (when did this happen?)
 */
public record OrderPlacedEvent(
    String eventId,
    OrderId orderId,
    CustomerId customerId,
    Money totalAmount,
    LocalDateTime occurredAt
) implements DomainEvent {

    /**
     * Compact constructor - validation for domain invariants
     */
    public OrderPlacedEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null or blank");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null in OrderPlacedEvent");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null in OrderPlacedEvent");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("Total amount cannot be null in OrderPlacedEvent");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred timestamp cannot be null in OrderPlacedEvent");
        }
    }

    /**
     * Convenience constructor for creating new events (generates ID and timestamp)
     */
    public OrderPlacedEvent(OrderId orderId, CustomerId customerId, Money totalAmount) {
        this(UUID.randomUUID().toString(), orderId, customerId, totalAmount, LocalDateTime.now());
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
