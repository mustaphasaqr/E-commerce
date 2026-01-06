package com.mustapha.ecommerce.ecommerce.shared.observability.paymentgateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.ecommerce.order.domain.model.valueobject.Money;

import java.util.Map;

/**
 * Logging Payment Decorator
 * Responsibility: Add logging to payment operations
 * Pattern: Decorator
 */
@Component
public class LoggingPaymentDecorator implements PaymentPort {

    private static final Logger logger = LoggerFactory.getLogger(LoggingPaymentDecorator.class);
    private final PaymentPort delegate;

    public LoggingPaymentDecorator(PaymentPort delegate) {
        this.delegate = delegate;
    }

    @Override
    public void processPayment(Money amount, Map<String, String> paymentDetails) {
        logger.info("Processing payment: amount={}", amount);
        try {
            delegate.processPayment(amount, paymentDetails);
            logger.info("Payment processed successfully: amount={}", amount);
        } catch (Exception e) {
            logger.error("Payment processing failed: amount={}", amount, e);
            throw e;
        }
    }
}
