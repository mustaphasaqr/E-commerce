package com.mustapha.ecommerce.order.domain.exception;

/**
 * Order Modification Not Allowed Exception
 * 
 * Domain Exception - thrown when attempting to modify an order that is not in a modifiable state.
 * 
 * Examples:
 * - Adding items to a confirmed order
 * - Removing items from a paid order
 * - Changing order content after shipment
 * 
 * Business Rule: Orders can only be modified when status.isModifiable() == true
 * 
 * Layer: DOMAIN (pure business logic)
 * HTTP Mapping: 409 Conflict (done by controller, not here)
 */
public class OrderModificationNotAllowedException extends RuntimeException {
    
    public OrderModificationNotAllowedException(String message) {
        super(message);
    }
    
    public OrderModificationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
