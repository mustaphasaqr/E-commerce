package com.mustapha.ecommerce.order.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Order Aggregate Root
 * Responsibility: Business rules, Invariants enforcement
 * Pattern: Aggregate Root, Builder
 * SOLID: SRP, OCP, LSP
 */
public class Order {
    private OrderId id;
    private String customerId;
    private List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Business rules
    public void addItem(OrderItem item) {
        validateItemCanBeAdded(item);
        this.items.add(item);
        recalculateTotal();
    }

    public void cancel() {
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order in status: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    // Invariants
    private void validateItemCanBeAdded(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Order item cannot be null");
        }
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot add items to order in status: " + this.status);
        }
    }

    private void recalculateTotal() {
        double total = items.stream()
                .mapToDouble(item -> item.getPrice().getAmount() * item.getQuantity())
                .sum();
        this.totalAmount = new Money(total);
    }

    // Getters and setters
    public OrderId getId() {
        return id;
    }

    public void setId(OrderId id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        recalculateTotal();
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
