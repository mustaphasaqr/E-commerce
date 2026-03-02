package com.mustapha.ecommerce.order.application.port;

import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Shipping Port
 * Responsibility: Outbound port for shipping operations
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation: ShippingAdapter (uses Aramex, DHL, etc.)
 */
public interface ShippingPort {

    /**
     * Create shipment with carrier and get tracking number
     * 
     * @param request Shipment details
     * @return Shipment result with tracking number
     */
    ShipmentResult createShipment(ShipmentRequest request);
    
    /**
     * Shipment request data
     */
    record ShipmentRequest(
        OrderId orderId,
        String recipientName,
        String recipientCompany,
        String recipientAddress1,
        String recipientAddress2,
        String recipientCity,
        String recipientState,
        String recipientPostalCode,
        String recipientCountry,
        String recipientPhone,
        String recipientEmail,
        double packageWeight,
        double packageLength,
        double packageWidth,
        double packageHeight,
        String packageDescription,
        double declaredValue,
        String serviceType // "EXPRESS", "STANDARD", etc.
    ) {}
    
    /**
     * Shipment result
     */
    record ShipmentResult(
        boolean success,
        String trackingNumber,
        String carrier,
        String labelUrl,
        String message
    ) {}
}

