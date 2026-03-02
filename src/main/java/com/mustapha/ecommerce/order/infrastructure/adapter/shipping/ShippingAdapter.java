package com.mustapha.ecommerce.order.infrastructure.adapter.shipping;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.ShippingPort;
import com.mustapha.ecommerce.order.infrastructure.adapter.shipping.sdk.ShippingProviderClient;

/**
 * Shipping Adapter
 * Responsibility: Implement ShippingPort using shipping provider (Aramex default)
 * Pattern: Adapter (Hexagonal Architecture)
 * 
 * Shipping Provider Coverage:
 * - Egypt ✅
 * - UAE ✅
 * - Saudi Arabia ✅
 * - Kuwait, Qatar, Bahrain, Oman, Jordan, Lebanon
 * 
 * Resilience Features:
 * - @Retry: Automatic retry on transient failures (3 attempts)
 * - @CircuitBreaker: Stops calling provider when failure rate exceeds 55%
 * - Graceful degradation: Returns MANUAL mode if provider unavailable
 * 
 * MOCK Mode:
 * - When credentials not configured
 * - Returns simulated tracking numbers
 * - Allows development without real API access
 */
@Component
public class ShippingAdapter implements ShippingPort {

    private static final Logger logger = LoggerFactory.getLogger(ShippingAdapter.class);
    private final ShippingProviderClient shippingClient;

    public ShippingAdapter(ShippingProviderClient shippingClient) {
        this.shippingClient = shippingClient;
    }

    /**
     * Create shipment with carrier
     * Falls back to MANUAL mode if API unavailable
     */
    @Override
    @Retry(name = "shippingService", fallbackMethod = "createShipmentFallback")
    @CircuitBreaker(name = "shippingService")
    public ShipmentResult createShipment(ShipmentRequest request) {
        try {
            logger.debug("Creating shipment: orderId={}, recipient={}", 
                        request.orderId().getValue(), request.recipientName());
            
            // Convert to provider request
            var providerRequest = new ShippingProviderClient.ShipmentRequest(
                null, // Sender from config (warehouse address)
                new ShippingProviderClient.Address(
                    request.recipientName(),
                    request.recipientCompany(),
                    request.recipientAddress1(),
                    request.recipientAddress2(),
                    request.recipientCity(),
                    request.recipientState(),
                    request.recipientPostalCode(),
                    request.recipientCountry(),
                    request.recipientPhone(),
                    request.recipientEmail()
                ),
                new ShippingProviderClient.PackageDetails(
                    request.packageWeight(),
                    request.packageLength(),
                    request.packageWidth(),
                    request.packageHeight(),
                    request.packageDescription(),
                    request.declaredValue()
                ),
                request.serviceType() != null ? request.serviceType() : "EXPRESS",
                false, // Signature not required by default
                request.orderId().getValue()
            );
            
            var response = shippingClient.createShipment(providerRequest);
            
            if (response.success()) {
                logger.info("✅ Shipment created: orderId={}, tracking={}", 
                           request.orderId().getValue(), response.trackingNumber());
                
                return new ShipmentResult(
                    true,
                    response.trackingNumber(),
                    "Aramex", // TODO: Make dynamic based on provider
                    response.labelUrl(),
                    "Shipment created successfully"
                );
            } else {
                logger.error("❌ Shipment creation failed: {}", response.errorMessage());
                return createManualShipmentResult(request, response.errorMessage() != null ? response.errorMessage() : "Unknown error");
            }
            
        } catch (Exception e) {
            logger.error("❌ Shipment creation exception: {}", e.getMessage(), e);
            return createManualShipmentResult(request, "Service unavailable: " + e.getMessage());
        }
    }
    
    /**
     * Fallback method when API fails
     */
    @SuppressWarnings("unused")
    private ShipmentResult createShipmentFallback(ShipmentRequest request, Exception ex) {
        logger.warn("⚠️ Shipping service fallback triggered: {}", ex.getMessage());
        return createManualShipmentResult(request, "Service temporarily unavailable");
    }
    
    /**
     * Create manual shipment result (graceful degradation)
     */
    private ShipmentResult createManualShipmentResult(ShipmentRequest request, String reason) {
        String manualTracking = "MANUAL-" + request.orderId().getValue();
        
        logger.info("📋 Manual shipment mode: orderId={}, tracking={}", 
                   request.orderId().getValue(), manualTracking);
        
        return new ShipmentResult(
            true, // Still successful, just manual
            manualTracking,
            "MANUAL",
            null,
            "Manual shipment mode - " + reason
        );
    }
}
