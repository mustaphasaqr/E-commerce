package com.mustapha.ecommerce.order.domain.event;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;

/**
 * Order Placed Domain Event
 * Responsibility: Represent the fact that an order was placed in the domain
 * Pattern: Domain Event
 * Layer: DOMAIN (not application!)
 */
public class OrderPlacedEvent {
    private final String orderId;
    private final Money totalAmount;

    public OrderPlacedEvent(String orderId, Money totalAmount) {
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
