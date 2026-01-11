package com.mustapha.ecommerce.order.application.validation;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.InventoryPort;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.exception.OrderValidationException;

/**
 * Stock Validator
 * Responsibility: Validate stock availability
 * Pattern: Chain of Responsibility
 */
@Component
public class StockValidator implements OrderValidator {

    private final InventoryPort inventoryPort;

    public StockValidator(InventoryPort inventoryPort) {
        this.inventoryPort = inventoryPort;
    }

    @Override
    public void validate(OrderRequest request) {
        boolean hasStock = inventoryPort.checkAvailability(request.getItems());
        if (!hasStock) {
            throw new OrderValidationException("Insufficient stock for order items");
        }
    }
}
