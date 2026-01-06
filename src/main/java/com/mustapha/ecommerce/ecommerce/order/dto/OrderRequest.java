package com.mustapha.ecommerce.ecommerce.order.dto;

import java.util.List;
import java.util.Map;

/**
 * Order Request DTO
 * Responsibility: API contract for order creation
 */
public class OrderRequest {
    private String customerId;
    private List<Map<String, Object>> items;
    private String paymentMethod;
    private Map<String, String> paymentDetails;

    // Constructors
    public OrderRequest() {
    }

    public OrderRequest(String customerId, List<Map<String, Object>> items, 
                       String paymentMethod, Map<String, String> paymentDetails) {
        this.customerId = customerId;
        this.items = items;
        this.paymentMethod = paymentMethod;
        this.paymentDetails = paymentDetails;
    }

    // Getters and setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Map<String, String> getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(Map<String, String> paymentDetails) {
        this.paymentDetails = paymentDetails;
    }
}
