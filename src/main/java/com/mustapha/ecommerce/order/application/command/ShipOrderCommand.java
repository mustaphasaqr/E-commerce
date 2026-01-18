package com.mustapha.ecommerce.order.application.command;

import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Ship Order Command
 * Responsibility: Transfer shipping data from API to application layer
 */
public class ShipOrderCommand {
    
    private final OrderId orderId;
    private final String trackingNumber;
    private final String carrier;
    
    public ShipOrderCommand(OrderId orderId, String trackingNumber, String carrier) {
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
    }
    
    public OrderId getOrderId() {
        return orderId;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public String getCarrier() {
        return carrier;
    }
}
