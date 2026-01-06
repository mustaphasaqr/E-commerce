package com.mustapha.ecommerce.ecommerce.order.application.validation;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.ecommerce.order.application.port.FraudCheckPort;
import com.mustapha.ecommerce.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.ecommerce.order.exception.OrderValidationException;

/**
 * Fraud Pre-Validator
 * Responsibility: Validate order for fraud detection
 * Pattern: Chain of Responsibility
 */
@Component
public class FraudPreValidator implements OrderValidator {

    private final FraudCheckPort fraudCheckPort;

    public FraudPreValidator(FraudCheckPort fraudCheckPort) {
        this.fraudCheckPort = fraudCheckPort;
    }

    @Override
    public void validate(OrderRequest request) {
        boolean isSuspicious = fraudCheckPort.checkFraud(request);
        if (isSuspicious) {
            throw new OrderValidationException("Order flagged for fraud");
        }
    }
}
