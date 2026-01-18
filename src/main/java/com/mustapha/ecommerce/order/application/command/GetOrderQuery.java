package com.mustapha.ecommerce.order.application.command;

import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Get Order Query
 * Responsibility: Query to retrieve order details
 * Pattern: CQRS - Query (read-only, no state changes)
 */
public class GetOrderQuery {
    
    private final OrderId orderId;
    
    public GetOrderQuery(OrderId orderId) {
        this.orderId = orderId;
    }
    
    public OrderId getOrderId() {
        return orderId;
    }
}
