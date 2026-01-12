package com.mustapha.ecommerce.order.domain.model;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;

/**
 * Order Item Entity
 * Responsibility: Represent a line item in an order
 * Domain Rules:
 * - Product ID cannot be null or empty
 * - Product name cannot be null or empty
 * - Quantity must be > 0
 * - Price cannot be null
 * - Immutable once created (no setters)
 */
public class OrderItem {
    private String productId;
    private String productName;
    private int quantity;
    private Money price;

    public OrderItem(String productId, String productName, int quantity, Money price) {
        // Validate all invariants
        validateProductId(productId);
        validateProductName(productName);
        validateQuantity(quantity);
        validatePrice(price);
        
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // ========== Domain Invariants ==========
    
    private void validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
    }
    
    private void validateProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
    }
    
    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }
    
    private void validatePrice(Money price) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        // Money should enforce its own rule: amount >= 0
    }

    /**
     * Calculate total for this line item
     * Uses Money.multiply() to preserve BigDecimal precision
     */
    public Money getTotal() {
        return price.multiply(quantity);
    }

    // Getters
    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getPrice() {
        return price;
    }
}
