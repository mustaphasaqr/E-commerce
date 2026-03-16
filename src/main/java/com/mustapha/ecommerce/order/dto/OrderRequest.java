package com.mustapha.ecommerce.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * Order Request DTO - Complete API Contract
 * Responsibility: API contract for order creation
 * 
 * Contains:
 * - Customer identification
 * - List of items to order (strongly typed)
 * - Payment information (optional - can pay later)
 * - Shipping address for geographic analytics
 * - Marketing UTM parameters for attribution analytics
 * - Cart ID for cart abandonment tracking
 */
public class OrderRequest {
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;
    
    // Shipping address for geographic analytics
    private String shippingCity;
    private String shippingState;
    private String shippingCountry;
    private String shippingZipCode;
    
    // Marketing attribution for analytics
    private String utmSource;
    private String utmCampaign;
    private String referrer;
    
    // Cart tracking for abandonment analytics
    private Long cartId;

    // Checkout-selected payment method
    private String paymentMethod;

    // Constructors
    public OrderRequest() {
    }

    public OrderRequest(String customerId, List<OrderItemRequest> items) {
        this.customerId = customerId;
        this.items = items;
    }

    // Getters and setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
    
    public String getShippingCity() {
        return shippingCity;
    }
    
    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }
    
    public String getShippingState() {
        return shippingState;
    }
    
    public void setShippingState(String shippingState) {
        this.shippingState = shippingState;
    }
    
    public String getShippingCountry() {
        return shippingCountry;
    }
    
    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }
    
    public String getShippingZipCode() {
        return shippingZipCode;
    }
    
    public void setShippingZipCode(String shippingZipCode) {
        this.shippingZipCode = shippingZipCode;
    }
    
    public String getUtmSource() {
        return utmSource;
    }
    
    public void setUtmSource(String utmSource) {
        this.utmSource = utmSource;
    }
    
    public String getUtmCampaign() {
        return utmCampaign;
    }
    
    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }
    
    public String getReferrer() {
        return referrer;
    }
    
    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
    
    public Long getCartId() {
        return cartId;
    }
    
    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
