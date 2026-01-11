package com.mustapha.ecommerce.order.dto;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;

/**
 * Order Response DTO
 * Responsibility: API contract for order response
 */
public class OrderResponse {
    private String orderId;
    private String customerId;
    private double totalAmount;
    private OrderStatus status;

    // Constructors
    public OrderResponse() {
    }

    public OrderResponse(String orderId, String customerId, double totalAmount, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId().getValue(),
                order.getCustomerId(),
                order.getTotalAmount().getAmount(),
                order.getStatus()
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
