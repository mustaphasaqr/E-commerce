package com.mustapha.ecommerce.order.domain.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Order Placed Domain Event
 * Responsibility: Immutable record that an order was placed
 * Pattern: Domain Event
 * Layer: DOMAIN (not application!)
 * 
 * Domain Rules:
 * - orderId must not be null (every event must reference a valid order)
 * - customerId must not be null (must know who placed the order)
 * - totalAmount must not be null (event must record actual amount)
 * - occurredAt timestamp is required (when did this happen?)
 * - Immutable once created (final class, final fields)
 */
public final class OrderPlacedEvent {
    private final String eventId;
    private final OrderId orderId;
    private final String customerId;
    private final Money totalAmount;
    private final LocalDateTime occurredAt;

    public OrderPlacedEvent(OrderId orderId, String customerId, Money totalAmount) {
        this(UUID.randomUUID().toString(), orderId, customerId, totalAmount, LocalDateTime.now());
    }
    
    // Constructor for event sourcing/rehydration
    public OrderPlacedEvent(String eventId, OrderId orderId, String customerId, Money totalAmount, LocalDateTime occurredAt) {
        validateEvent(eventId, orderId, customerId, totalAmount, occurredAt);
        
        this.eventId = eventId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.occurredAt = occurredAt;
    }
    
    // ========== Domain Invariants ==========
    
    private void validateEvent(String eventId, OrderId orderId, String customerId, Money totalAmount, LocalDateTime occurredAt) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null in OrderPlacedEvent");
        }
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty in OrderPlacedEvent");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("Total amount cannot be null in OrderPlacedEvent");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred timestamp cannot be null in OrderPlacedEvent");
        }
    }

    // ========== Getters ==========
    
    public String getEventId() {
        return eventId;
    }

    public OrderId getOrderId() {
        return orderId;
    }
    
    public String getCustomerId() {
        return customerId;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }
    
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    // ========== Value Object Methods ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderPlacedEvent that = (OrderPlacedEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "OrderPlacedEvent{" +
                "eventId='" + eventId + '\'' +
                ", orderId=" + orderId +
                ", customerId='" + customerId + '\'' +
                ", totalAmount=" + totalAmount +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
