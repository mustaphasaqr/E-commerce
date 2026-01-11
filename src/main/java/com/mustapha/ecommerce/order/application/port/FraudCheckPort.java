package com.mustapha.ecommerce.order.application.port;

import com.mustapha.ecommerce.order.dto.OrderRequest;

/**
 * Fraud Check Port - Hexagonal Architecture
 * Responsibility: Define contract for fraud detection
 */
public interface FraudCheckPort {
    boolean checkFraud(OrderRequest request);
}
