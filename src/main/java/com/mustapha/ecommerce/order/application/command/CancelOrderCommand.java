package com.mustapha.ecommerce.order.application.command;

import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Cancel Order Command
 * Responsibility: Transfer cancellation data from API to application layer
 */
public class CancelOrderCommand {
    
    private final OrderId orderId;
    private final String reason;
    
    public CancelOrderCommand(OrderId orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }
    
    public OrderId getOrderId() {
        return orderId;
    }
    
    public String getReason() {
        return reason;
    }
}
