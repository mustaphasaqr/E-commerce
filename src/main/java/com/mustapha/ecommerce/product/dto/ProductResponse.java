package com.mustapha.ecommerce.product.dto;

import com.mustapha.ecommerce.product.domain.model.Product;

import java.math.BigDecimal;

/**
 * Product Response DTO - Complete API Contract
 * Responsibility: Full product information for API consumers
 * 
 * Contains:
 * - Product identification (id, sku)
 * - Product details (name, description)
 * - Pricing information (price, currency)
 * - Stock information (total, available, reserved)
 * - Product state (active, visible, availableForPurchase, discontinued)
 */
public class ProductResponse {
    private String id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private int totalStock;
    private int availableStock;
    private int reservedStock;
    private boolean active;
    private boolean visible;
    private boolean availableForPurchase;
    private boolean discontinued;

    // Constructors
    public ProductResponse() {
    }

    public ProductResponse(String id, String sku, String name, String description,
                          BigDecimal price, String currency, int totalStock, int availableStock,
                          int reservedStock, boolean active, boolean visible,
                          boolean availableForPurchase, boolean discontinued) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.totalStock = totalStock;
        this.availableStock = availableStock;
        this.reservedStock = reservedStock;
        this.active = active;
        this.visible = visible;
        this.availableForPurchase = availableForPurchase;
        this.discontinued = discontinued;
    }

    public static ProductResponse fromDomain(Product product) {
        return new ProductResponse(
            product.getId().getValue(),
            product.getSku().getValue(),
            product.getName(),
            product.getDescription(),
            product.getPrice().getAmount(),
            product.getPrice().getCurrency().getCurrencyCode(),
            product.getStock().getQuantity(),
            product.getStock().getAvailableQuantity(),
            product.getStock().getReservedQuantity(),
            product.isActive(),
            product.isVisible(),
            product.isAvailableForPurchase(),
            product.isDiscontinued()
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public int getReservedStock() {
        return reservedStock;
    }

    public void setReservedStock(int reservedStock) {
        this.reservedStock = reservedStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isAvailableForPurchase() {
        return availableForPurchase;
    }

    public void setAvailableForPurchase(boolean availableForPurchase) {
        this.availableForPurchase = availableForPurchase;
    }

    public boolean isDiscontinued() {
        return discontinued;
    }

    public void setDiscontinued(boolean discontinued) {
        this.discontinued = discontinued;
    }
}

