package com.mustapha.ecommerce.order.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.mustapha.ecommerce.order.domain.model.Order;

/**
 * Order Response DTO - Complete API Contract
 * Responsibility: Full order information for API consumers
 * 
 * Contains:
 * - Order identification (orderId, customerId)
 * - Order items (products, quantities, prices)
 * - Financial data (totalAmount)
 * - Status tracking (status, timestamps)
 * - Shipping information (when shipped)
 * - Cancellation information (when cancelled)
 */
public class OrderResponse {
    private String orderId;
    private String customerId;
    private List<OrderItemResponse> items;
    private double totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Shipping information (populated when status = SHIPPED or DELIVERED)
    private String trackingNumber;
    private String carrier;

    // Checkout-selected payment method
    private String paymentMethod;
    
    // Delivery information (populated when status = DELIVERED)
    private LocalDateTime deliveredAt;
    
    // Cancellation information (populated when status = CANCELLED)
    private String cancellationReason;

    // Constructors
    public OrderResponse() {
    }

    public OrderResponse(String orderId, String customerId, List<OrderItemResponse> items,
                        double totalAmount, String status, LocalDateTime createdAt, 
                        LocalDateTime updatedAt, String trackingNumber, String carrier, String paymentMethod,
                        LocalDateTime deliveredAt, String cancellationReason) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.paymentMethod = paymentMethod;
        this.deliveredAt = deliveredAt;
        this.cancellationReason = cancellationReason;
    }

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
            .map(OrderItemResponse::from)
            .collect(Collectors.toList());
            
        return new OrderResponse(
            order.getId().getValue(),
            order.getCustomerId().getValue(),
            itemResponses,
            order.getTotalAmount().getAmount(),
            order.getStatus().name(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getTrackingNumber(),
            order.getCarrier(),
            order.getPaymentMethod(),
            order.getDeliveredAt(),
            order.getCancellationReason()
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

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
