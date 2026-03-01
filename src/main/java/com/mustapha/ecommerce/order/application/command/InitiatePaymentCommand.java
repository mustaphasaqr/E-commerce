package com.mustapha.ecommerce.order.application.command;

import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Initiate Payment Command
 * Responsibility: Transfer payment initiation data from API to application layer
 * 
 * Pattern: Command (CQRS)
 */
public class InitiatePaymentCommand {
    
    private final OrderId orderId;
    private final String paymentMethod; // VISA, MASTERCARD, MADA
    private final String customerEmail;
    private final String shopperResultUrl; // URL to redirect customer after payment
    
    public InitiatePaymentCommand(
            OrderId orderId, 
            String paymentMethod, 
            String customerEmail,
            String shopperResultUrl) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.customerEmail = customerEmail;
        this.shopperResultUrl = shopperResultUrl;
    }
    
    public OrderId getOrderId() {
        return orderId;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public String getShopperResultUrl() {
        return shopperResultUrl;
    }
}
