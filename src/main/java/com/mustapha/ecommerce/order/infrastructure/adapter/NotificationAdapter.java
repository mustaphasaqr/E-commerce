package com.mustapha.ecommerce.order.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Notification Adapter - Stub implementation for development
 */
@Component
public class NotificationAdapter implements NotificationPort {

    @Override
    public void sendOrderConfirmation(CustomerId customerId, OrderId orderId) {
        // Stub: In production, this would send email/SMS
        System.out.println("Notification: Order confirmed - Customer: " + customerId.getValue() + 
                          ", Order: " + orderId.getValue());
    }

    @Override
    public void sendOrderShipped(CustomerId customerId, OrderId orderId, String trackingNumber, String carrier) {
        // Stub: In production, this would send email/SMS
        System.out.println("Notification: Order shipped - Customer: " + customerId.getValue() + 
                          ", Order: " + orderId.getValue() + ", Tracking: " + trackingNumber + 
                          ", Carrier: " + carrier);
    }

    @Override
    public void sendOrderDelivered(CustomerId customerId, OrderId orderId) {
        // Stub: In production, this would send email/SMS
        System.out.println("Notification: Order delivered - Customer: " + customerId.getValue() + 
                          ", Order: " + orderId.getValue());
    }

    @Override
    public void sendOrderCancelled(CustomerId customerId, OrderId orderId, String reason) {
        // Stub: In production, this would send email/SMS
        System.out.println("Notification: Order cancelled - Customer: " + customerId.getValue() + 
                          ", Order: " + orderId.getValue() + ", Reason: " + reason);
    }

    @Override
    public void sendPaymentReceived(CustomerId customerId, OrderId orderId) {
        // Stub: In production, this would send email/SMS
        System.out.println("Notification: Payment received - Customer: " + customerId.getValue() + 
                          ", Order: " + orderId.getValue());
    }
}
