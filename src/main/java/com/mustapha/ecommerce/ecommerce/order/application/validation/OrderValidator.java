package com.mustapha.ecommerce.ecommerce.order.application.validation;

import com.mustapha.ecommerce.ecommerce.order.dto.OrderRequest;

/**
 * Order Validator Interface
 * Pattern: Chain of Responsibility
 * SOLID: SRP, OCP
 */
public interface OrderValidator {
    void validate(OrderRequest request);
}
