package com.mustapha.ecommerce.order.infrastructure.adapter.shipping.sdk;

import java.util.List;

/**
 * Shipping Provider Client Interface
 * Responsibility: Common contract for all shipping providers (Aramex, DHL, FedEx, etc.)
 * Pattern: Interface Segregation + Adapter Pattern
 * 
 * Similar to PaymentGatewayClient for payments
 */
public interface ShippingProviderClient {

    /**
     * Create shipment with carrier
     * 
     * @param request Shipment details (sender, recipient, package info)
     * @return Shipment response with tracking number and label URL
     */
    ShipmentResponse createShipment(ShipmentRequest request);
    
    /**
     * Track existing shipment
     * 
     * @param trackingNumber Tracking number from carrier
     * @return Current shipment status and location
     */
    TrackingResponse trackShipment(String trackingNumber);
    
    /**
     * Get shipping rates (optional - for rate shopping)
     * 
     * @param request Rate request with origin, destination, package details
     * @return List of available services with rates
     */
    RateResponse getShippingRates(RateRequest request);
    
    /**
     * Cancel shipment (if supported by carrier)
     * 
     * @param trackingNumber Tracking number to cancel
     * @return Cancellation confirmation
     */
    CancellationResponse cancelShipment(String trackingNumber);
    
    // ========================================
    // Request/Response DTOs
    // ========================================
    
    /**
     * Shipment creation request
     */
    record ShipmentRequest(
        Address sender,
        Address recipient,
        PackageDetails packageDetails,
        String serviceType,  // e.g., "EXPRESS", "STANDARD"
        boolean requiresSignature,
        String reference      // Order ID or custom reference
    ) {}
    
    /**
     * Address information
     */
    record Address(
        String name,
        String company,
        String address1,
        String address2,
        String city,
        String state,
        String postalCode,
        String country,       // ISO 3166-1 alpha-2 code (EG, SA, AE)
        String phone,
        String email
    ) {}
    
    /**
     * Package details
     */
    record PackageDetails(
        double weight,        // In kilograms
        double length,        // In centimeters
        double width,         // In centimeters
        double height,        // In centimeters
        String description,
        double declaredValue  // For insurance
    ) {}
    
    /**
     * Shipment creation response
     */
    record ShipmentResponse(
        boolean success,
        String trackingNumber,
        String labelUrl,      // URL to download shipping label
        String carrierCode,   // Carrier-specific code
        String estimatedDeliveryDate,
        String errorMessage
    ) {}
    
    /**
     * Tracking response
     */
    record TrackingResponse(
        boolean success,
        String trackingNumber,
        ShipmentStatus status,
        String currentLocation,
        String estimatedDeliveryDate,
        List<TrackingEvent> events,
        String errorMessage
    ) {}
    
    /**
     * Shipment status enum
     */
    enum ShipmentStatus {
        CREATED,
        PICKED_UP,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        EXCEPTION,
        CANCELLED,
        RETURNED
    }
    
    /**
     * Tracking event (checkpoint)
     */
    record TrackingEvent(
        String timestamp,
        String location,
        String status,
        String description
    ) {}
    
    /**
     * Rate request
     */
    record RateRequest(
        Address origin,
        Address destination,
        PackageDetails packageDetails
    ) {}
    
    /**
     * Rate response
     */
    record RateResponse(
        boolean success,
        List<ServiceRate> rates,
        String errorMessage
    ) {}
    
    /**
     * Service rate option
     */
    record ServiceRate(
        String serviceType,
        String serviceName,
        double cost,
        String currency,
        String estimatedDeliveryDate
    ) {}
    
    /**
     * Cancellation response
     */
    record CancellationResponse(
        boolean success,
        String message
    ) {}
}
