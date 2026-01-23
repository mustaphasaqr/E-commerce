package com.mustapha.ecommerce.product.application.command;

import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;

/**
 * Create Product Command (Input DTO)
 * Responsibility: Transfer data from API layer to Application layer
 * Pattern: Command (CQS - Command Query Separation)
 * 
 * Note: Uses value objects (SKU, Price, Stock) for type safety
 * The Facade converts primitives from API → value objects for this command
 */
public class CreateProductCommand {
    
    private final SKU sku;
    private final String name;
    private final String description;
    private final Price price;
    private final Stock stock;
    
    public CreateProductCommand(SKU sku, String name, String description, Price price, Stock stock) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
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
}
