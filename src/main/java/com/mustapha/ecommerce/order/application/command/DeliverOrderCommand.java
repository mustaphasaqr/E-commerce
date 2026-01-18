package com.mustapha.ecommerce.order.application.command;

import java.time.LocalDateTime;

import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Deliver Order Command
 * Responsibility: Transfer delivery data from API to application layer
 */
public class DeliverOrderCommand {
    
    private final OrderId orderId;
    private final LocalDateTime deliveredAt;
    
    public DeliverOrderCommand(OrderId orderId, LocalDateTime deliveredAt) {
        this.orderId = orderId;
        this.deliveredAt = deliveredAt;
    }
    
    public OrderId getOrderId() {
        return orderId;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
}
