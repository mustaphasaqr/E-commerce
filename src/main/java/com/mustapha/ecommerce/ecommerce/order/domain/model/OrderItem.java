package com.mustapha.ecommerce.ecommerce.order.domain.model;

import com.mustapha.ecommerce.ecommerce.order.domain.model.valueobject.Money;

/**
 * Order Item Entity
 * Responsibility: Represent a line item in an order
 */
public class OrderItem {
    private String productId;
    private String productName;
    private int quantity;
    private Money price;

    public OrderItem(String productId, String productName, int quantity, Money price) {
        validateQuantity(quantity);
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    public Money getTotal() {
        return new Money(price.getAmount() * quantity);
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
