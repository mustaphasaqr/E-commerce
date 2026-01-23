package com.mustapha.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Product Request DTO - Complete API Contract
 * Responsibility: API contract for product creation
 * 
 * Contains:
 * - Product identification (SKU)
 * - Product details (name, description)
 * - Pricing information (price, currency)
 * - Initial stock quantity
 */
public class ProductRequest {
    
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @NotBlank(message = "Currency code is required")
    private String currencyCode;
    
    @NotNull(message = "Initial stock is required")
    @PositiveOrZero(message = "Initial stock cannot be negative")
    private Integer initialStock;

    // Constructors
    public ProductRequest() {
    }

    public ProductRequest(String sku, String name, String description, 
                         BigDecimal price, String currencyCode, Integer initialStock) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.currencyCode = currencyCode;
        this.initialStock = initialStock;
    }

    // Getters and setters
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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Integer getInitialStock() {
        return initialStock;
    }

    public void setInitialStock(Integer initialStock) {
        this.initialStock = initialStock;
    }
}
