package com.mustapha.ecommerce.order.application.port;

import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Notification Port (Outbound Port)
 * Responsibility: Send notifications to customers
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation will be in Infrastructure layer
 * Examples: EmailService, SMSService, PushNotificationService
 */
public interface NotificationPort {
    
    /**
     * Send order confirmation notification
     * Triggered after order is successfully placed
     * 
     * @param customerId Customer to notify
     * @param orderId Order that was placed
     */
    void sendOrderConfirmation(CustomerId customerId, OrderId orderId);
    
    /**
     * Send order shipped notification
     * Triggered when order status changes to SHIPPED
     * 
     * @param customerId Customer to notify
     * @param orderId Order that was shipped
     * @param trackingNumber Shipping tracking number
     * @param carrier Shipping carrier (e.g., "FedEx", "UPS", "DHL")
     */
    void sendOrderShipped(CustomerId customerId, OrderId orderId, String trackingNumber, String carrier);
    
    /**
     * Send order delivered notification
     * Triggered when order status changes to DELIVERED
     * 
     * @param customerId Customer to notify
     * @param orderId Order that was delivered
     */
    void sendOrderDelivered(CustomerId customerId, OrderId orderId);
    
    /**
     * Send order cancellation notification
     * Triggered when order is cancelled
     * 
     * @param customerId Customer to notify
     * @param orderId Order that was cancelled
     * @param reason Cancellation reason
     */
    void sendOrderCancelled(CustomerId customerId, OrderId orderId, String reason);
    
    /**
     * Send payment received notification
     * Triggered when payment is successfully processed
     * 
     * @param customerId Customer to notify
     * @param orderId Order that was paid
     */
    void sendPaymentReceived(CustomerId customerId, OrderId orderId);
}
