package com.mustapha.ecommerce.order.domain.event;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;

/**
 * Order Placed Domain Event
 * Responsibility: Represent the fact that an order was placed in the domain
 * Pattern: Domain Event
 * Layer: DOMAIN (not application!)
 * Invariants: 
 * - orderId must not be null or empty (every event must reference a valid order)
 * - totalAmount must not be null (event must record actual amount)
 */
public class OrderPlacedEvent {
    private final String orderId;
    private final Money totalAmount;

    public OrderPlacedEvent(String orderId, Money totalAmount) {
        // Domain Rule: Events must represent valid business facts
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty in OrderPlacedEvent");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("Total amount cannot be null in OrderPlacedEvent");
        }
        
        this.orderId = orderId;
        this.totalAmount = totalAmount;
    }

    public String getOrderId() {
        return orderId;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }
}
