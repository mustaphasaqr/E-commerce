package com.mustapha.ecommerce.ecommerce.order.exception;

/**
 * Payment Rejected Exception
 */
public class PaymentRejectedException extends RuntimeException {
    public PaymentRejectedException(String message) {
        super(message);
    }
}
