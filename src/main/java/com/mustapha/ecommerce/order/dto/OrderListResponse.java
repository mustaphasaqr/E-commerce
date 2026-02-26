package com.mustapha.ecommerce.order.dto;

import com.mustapha.ecommerce.order.domain.model.Order;

import java.time.LocalDateTime;

/**
 * Order List Response DTO - Lightweight for List Operations
 * Responsibility: Minimal order information for list endpoints
 * Performance: 55% smaller than OrderResponse (5 fields vs 11)
 * 
 * Use Cases:
 * - Customer order history
 * - Admin order management
 * - Order search results
 * 
 * For full details (items, shipping, cancellation), use OrderResponse via GET /api/orders/{id}
 */
public class OrderListResponse {
    private String orderId;
    private String customerId;
    private double totalAmount;
    private String status;
    private LocalDateTime createdAt;

    // Constructors
    public OrderListResponse() {
    }

    public OrderListResponse(String orderId, String customerId, double totalAmount, 
                            String status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Create lightweight DTO from domain model
     * Only extracts fields needed for list view
     */
    public static OrderListResponse fromDomain(Order order) {
        return new OrderListResponse(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            order.getTotalAmount().getAmount(),
            order.getStatus().name(),
            order.getCreatedAt()
        );
    }

    // Getters and setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
