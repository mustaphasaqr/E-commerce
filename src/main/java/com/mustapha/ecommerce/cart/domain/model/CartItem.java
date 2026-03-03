package com.mustapha.ecommerce.cart.domain.model;

import com.mustapha.ecommerce.cart.domain.model.valueobject.Money;
import com.mustapha.ecommerce.cart.domain.model.valueobject.ProductId;

/**
 * Cart item value object representing a line item in the shopping cart.
 * 
 * Domain Rules:
 * - Product ID cannot be null (enforced by ProductId value object)
 * - Product name cannot be blank
 * - Quantity must be positive
 * - Price cannot be negative (enforced by Money value object)
 */
public class CartItem {
    
    private Long id; // Technical ID for ORM (not a domain identifier)
    private ProductId productId;
    private String productName;
    private int quantity;
    private Money price;
    
    /**
     * Constructor for creating new cart items
     */
    public CartItem(ProductId productId, String productName, int quantity, Money price) {
        validateProductName(productName);
        validateQuantity(quantity);
        
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
    
    /**
     * Full constructor for loading from persistence layer
     */
    public CartItem(Long id, ProductId productId, String productName, int quantity, Money price) {
        this(productId, productName, quantity, price);
        this.id = id;
    }
    
    /**
     * Get subtotal for this cart item (domain operation)
     */
    public Money getSubtotal() {
        return price.multiply(quantity);
    }
    
    /**
     * Increment quantity by specified amount
     */
    public void incrementQuantity(int amount) {
        validateQuantity(amount);
        this.quantity += amount;
    }
    
    /**
     * Set new quantity
     */
    public void setQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }
    
    // ========== Domain Validation ==========
    
    private void validateProductName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
    }
    
    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
    }
    
    // ========== Getters ==========
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ProductId getProductId() {
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
