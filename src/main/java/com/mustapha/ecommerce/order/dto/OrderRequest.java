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
 */
public class OrderRequest {
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

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
}
