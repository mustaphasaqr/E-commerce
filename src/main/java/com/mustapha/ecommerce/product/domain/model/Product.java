package com.mustapha.ecommerce.product.domain.model;

import com.mustapha.ecommerce.product.domain.event.ProductCreatedEvent;
import com.mustapha.ecommerce.product.domain.event.StockUpdatedEvent;
import com.mustapha.ecommerce.product.domain.exception.InsufficientStockException;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product Aggregate Root
 * Responsibility: Product business logic with invariants protection
 * Pattern: DDD Aggregate Root
 */
public class Product {
    private final ProductId id;
    private final SKU sku;
    private String name;
    private String description;
    private Price price;
    private Stock stock;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private final List<Object> domainEvents = new ArrayList<>();

    // Private constructor for invariants protection
    private Product(ProductId id, SKU sku, String name, String description, 
                   Price price, Stock stock, boolean active, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory method: Create new product
     */
    public static Product create(SKU sku, String name, String description, Price price, Stock stock) {
        validateProductName(name);
        
        Product product = new Product(
            ProductId.generate(),
            sku,
            name,
            description,
            price,
            stock,
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        product.domainEvents.add(new ProductCreatedEvent(product.id, sku.getValue(), name));
        return product;
    }

    /**
     * Factory method: Reconstitute from database
     */
    public static Product reconstitute(ProductId id, SKU sku, String name, String description,
                                      Price price, Stock stock, boolean active,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Product(id, sku, name, description, price, stock, active, createdAt, updatedAt);
    }

    // Business logic methods
    
    /**
     * Reserve stock for an order
     */
    public void reserveStock(int quantity) {
        if (!active) {
            throw new IllegalStateException("Cannot reserve stock for inactive product: " + id.getValue());
        }
        
        int previousQuantity = stock.getQuantity();
        this.stock = stock.reserve(quantity);
        this.updatedAt = LocalDateTime.now();
        
        domainEvents.add(new StockUpdatedEvent(id, previousQuantity, stock.getQuantity()));
    }

    /**
     * Restock inventory
     */
    public void restock(int quantity) {
        int previousQuantity = stock.getQuantity();
        this.stock = stock.restock(quantity);
        this.updatedAt = LocalDateTime.now();
        
        domainEvents.add(new StockUpdatedEvent(id, previousQuantity, stock.getQuantity()));
    }

    /**
     * Update product price
     */
    public void updatePrice(Price newPrice) {
        if (newPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update product details
     */
    public void updateDetails(String name, String description) {
        validateProductName(name);
        this.name = name;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if stock is available for requested quantity
     */
    public boolean isStockAvailable(int quantity) {
        return active && stock.hasQuantity(quantity);
    }

    /**
     * Deactivate product
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Activate product
     */
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    // Validation
    
    private static void validateProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("Product name cannot exceed 200 characters");
        }
    }

    // Getters (no setters - encapsulation)
    
    public ProductId getId() {
        return id;
    }

    public SKU getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Price getPrice() {
        return price;
    }

    public Stock getStock() {
        return stock;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<Object> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
