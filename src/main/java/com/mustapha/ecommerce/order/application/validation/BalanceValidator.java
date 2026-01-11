package com.mustapha.ecommerce.order.application.validation;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.dto.OrderRequest;

/**
 * Balance Validator
 * Responsibility: Validate customer balance
 * Pattern: Chain of Responsibility
 */
@Component
public class BalanceValidator implements OrderValidator {

    @Override
    public void validate(OrderRequest request) {
        // Implement balance validation logic
    }
}
