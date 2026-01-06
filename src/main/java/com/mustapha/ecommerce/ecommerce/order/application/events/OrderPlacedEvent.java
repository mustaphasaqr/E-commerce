package com.mustapha.ecommerce.ecommerce.order.application.events;

import com.mustapha.ecommerce.ecommerce.order.domain.model.valueobject.Money;

/**
 * Order Placed Event
 * Pattern: Domain Event, Observer
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
