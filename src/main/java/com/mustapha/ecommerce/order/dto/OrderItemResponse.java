package com.mustapha.ecommerce.order.dto;

import com.mustapha.ecommerce.order.domain.model.OrderItem;

/**
 * Order Item Response DTO
 * Responsibility: API contract for individual order item
 */
public class OrderItemResponse {
    private String productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double subtotal;

    public OrderItemResponse() {
    }

    public OrderItemResponse(String productId, String productName, int quantity, double unitPrice, double subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
            item.getProductId().getValue(),
            item.getProductName(),
            item.getQuantity(),
            item.getPrice().getAmount(),      // price = unit price
            item.getTotal().getAmount()       // total = subtotal for this item
        );
    }

    // Getters and setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
