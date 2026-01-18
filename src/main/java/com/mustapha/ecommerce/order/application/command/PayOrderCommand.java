package com.mustapha.ecommerce.order.application.command;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Pay Order Command
 * Responsibility: Transfer payment data from API to application layer
 */
public class PayOrderCommand {
    
    private final OrderId orderId;
    private final String paymentMethod;
    private final String paymentToken;
    private final Money amount;
    
    public PayOrderCommand(OrderId orderId, String paymentMethod, String paymentToken, Money amount) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentToken = paymentToken;
        this.amount = amount;
    }
    
    public OrderId getOrderId() {
        return orderId;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public String getPaymentToken() {
        return paymentToken;
    }
    
    public Money getAmount() {
        return amount;
    }
}
