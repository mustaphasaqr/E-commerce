package com.mustapha.ecommerce.order.application.command;

/**
 * Verify Payment Command
 * Responsibility: Transfer payment verification data from API to application layer
 * 
 * Pattern: Command (CQRS)
 */
public class VerifyPaymentCommand {
    
    private final String checkoutId;
    
    public VerifyPaymentCommand(String checkoutId) {
        this.checkoutId = checkoutId;
    }
    
    public String getCheckoutId() {
        return checkoutId;
    }
}
