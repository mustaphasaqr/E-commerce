package com.mustapha.ecommerce.order.application.port.fraudcheck;

import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Fraud Check Port (Outbound Port)
 * Responsibility: Fraud detection and risk assessment
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation will be in Infrastructure layer
 * Examples: Stripe Radar, PayPal Fraud Protection, Custom ML Model
 */
public interface FraudCheckPort {
    
    /**
     * Check order for fraud risk
     * 
     * @param orderId Order being checked
     * @param customerId Customer placing the order
     * @param amount Order total amount
     * @return Risk assessment result
     */
    FraudCheckResult checkOrder(OrderId orderId, CustomerId customerId, Money amount);
}
