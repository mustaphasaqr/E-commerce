package com.mustapha.ecommerce.order.infrastructure.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort.CheckoutResult;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentVerificationResult;
import com.mustapha.ecommerce.order.application.port.PaymentPort.RefundResult;
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
    public CheckoutResult createCheckout(OrderId orderId, Money amount, String paymentMethod, String customerEmail) {
        logger.info("Creating payment checkout for order: {}, amount: {}, method: {}, email: {}", 
                    orderId.getValue(), amount.getAmount(), paymentMethod, customerEmail);
        try {
            CheckoutResult result = delegate.createCheckout(orderId, amount, paymentMethod, customerEmail);
            if (result.success()) {
                logger.info("Checkout created successfully: checkoutId={}, redirectUrl={}", 
                            result.checkoutId(), result.redirectUrl());
            } else {
                logger.warn("Checkout creation failed: {}", result.message());
            }
            return result;
        } catch (Exception e) {
            logger.error("Checkout creation exception: orderId={}, amount={}", orderId.getValue(), amount.getAmount(), e);
            throw e;
        }
    }

    @Override
    public PaymentVerificationResult verifyPayment(String checkoutId) {
        logger.info("Verifying payment for checkoutId: {}", checkoutId);
        try {
            PaymentVerificationResult result = delegate.verifyPayment(checkoutId);
            logger.info("Payment verification completed: status={}, transactionId={}", 
                        result.status(), result.transactionId());
            return result;
        } catch (Exception e) {
            logger.error("Payment verification exception: checkoutId={}", checkoutId, e);
            throw e;
        }
    }

    @Override
    public RefundResult refundPayment(OrderId orderId, String transactionId, Money amount, String reason) {
        logger.info("Processing refund for order: {}, transactionId: {}, amount: {}, reason: {}", 
                    orderId.getValue(), transactionId, amount.getAmount(), reason);
        try {
            RefundResult result = delegate.refundPayment(orderId, transactionId, amount, reason);
            if (result.success()) {
                logger.info("Refund processed successfully: refundId={}", result.refundId());
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
