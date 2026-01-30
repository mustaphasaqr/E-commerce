package com.mustapha.ecommerce.order.infrastructure.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Logging Payment Decorator
 * Responsibility: Add logging to payment operations
 * Pattern: Decorator (Transparent logging wrapper)
 * 
 * Location: Order BC infrastructure (decorates Order's port to payment gateway)
 */
@Component
public class LoggingPaymentDecorator implements PaymentPort {

    private static final Logger logger = LoggerFactory.getLogger(LoggingPaymentDecorator.class);
    private final PaymentPort delegate;

    public LoggingPaymentDecorator(@Qualifier("paymentAdapter") PaymentPort delegate) {
        this.delegate = delegate;
    }

    @Override
    public PaymentResult processPayment(OrderId orderId, Money amount, String paymentMethod, String paymentToken) {
        logger.info("Processing payment for order: {}, amount: {}, method: {}", 
                    orderId.getValue(), amount.getAmount(), paymentMethod);
        try {
            PaymentResult result = delegate.processPayment(orderId, amount, paymentMethod, paymentToken);
            if (result.isSuccess()) {
                logger.info("Payment processed successfully: transactionId={}", result.transactionId());
            } else {
                logger.warn("Payment failed: {}", result.message());
            }
            return result;
        } catch (Exception e) {
            logger.error("Payment processing exception: orderId={}, amount={}", orderId.getValue(), amount.getAmount(), e);
            throw e;
        }
    }

    @Override
    public PaymentResult refundPayment(OrderId orderId, Money amount) {
        logger.info("Processing refund for order: {}, amount: {}", orderId.getValue(), amount.getAmount());
        try {
            PaymentResult result = delegate.refundPayment(orderId, amount);
            if (result.isSuccess()) {
                logger.info("Refund processed successfully: transactionId={}", result.transactionId());
            } else {
                logger.warn("Refund failed: {}", result.message());
            }
            return result;
        } catch (Exception e) {
            logger.error("Refund processing exception: orderId={}, amount={}", orderId.getValue(), amount.getAmount(), e);
            throw e;
        }
    }
}
