package com.mustapha.ecommerce.order.infrastructure.exception;

import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Infrastructure Exception - Order Not Found
 * 
 * Thrown when an order cannot be found in the repository by its ID.
 * This is a technical concern, not a business rule violation.
 */
public class OrderNotFoundException extends RuntimeException {
    
    private final OrderId orderId;
    
    public OrderNotFoundException(OrderId orderId) {
        super("Order not found with ID: " + orderId.getValue());
        this.orderId = orderId;
    }
    
    public OrderId getOrderId() {
        return orderId;
    }
}
