package com.mustapha.ecommerce.product.dto;

import com.mustapha.ecommerce.product.domain.model.Product;

import java.math.BigDecimal;

/**
 * Product List Response DTO - Lightweight for List Operations
 * Responsibility: Minimal product information for list/search endpoints
 * Performance: 46% smaller than ProductResponse (7 fields vs 13)
 * 
 * Use Cases:
 * - Product catalog listing
 * - Search results
 * - Product browsing
 * 
 * For full details, use ProductResponse via GET /api/products/{id}
 */
public class ProductListResponse {
    private String id;
    private String sku;
    private String name;
    private BigDecimal price;
    private String currency;
    private int availableStock;
    private boolean active;

    // Constructors
    public ProductListResponse() {
    }

    public ProductListResponse(String id, String sku, String name, 
                               BigDecimal price, String currency, 
                               int availableStock, boolean active) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.availableStock = availableStock;
        this.active = active;
    }

    /**
     * Create lightweight DTO from domain model
     * Only extracts fields needed for list view
     */
    public static ProductListResponse fromDomain(Product product) {
        return new ProductListResponse(
            product.getId().getValue(),
            product.getSku().getValue(),
            product.getName(),
            product.getPrice().getAmount(),
            product.getPrice().getCurrency().getCurrencyCode(),
            product.getStock().getAvailableQuantity(),
            product.isActive()
        );
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
