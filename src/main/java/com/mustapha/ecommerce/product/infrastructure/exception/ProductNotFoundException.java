package com.mustapha.ecommerce.product.infrastructure.exception;

import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;

/**
 * Infrastructure Exception - Product Not Found
 * 
 * Thrown when a product cannot be found in the repository.
 * This is a technical/persistence concern, not a business rule violation.
 * 
 * Pattern: Matches OrderNotFoundException in Order bounded context
 */
public final class ProductNotFoundException extends RuntimeException {
    
    private final String identifier;
    private final String identifierType;
    
    /**
     * Constructor for ProductId lookup failure
     */
    public ProductNotFoundException(ProductId productId) {
        super("Product not found with ID: " + productId.getValue());
        this.identifier = productId.getValue();
        this.identifierType = "ID";
    }
    
    /**
     * Constructor for SKU lookup failure
     */
    public ProductNotFoundException(SKU sku) {
        super("Product not found with SKU: " + sku.getValue());
        this.identifier = sku.getValue();
        this.identifierType = "SKU";
    }
    
    /**
     * Constructor for ProductId lookup failure with cause
     */
    public ProductNotFoundException(ProductId productId, Throwable cause) {
        super("Product not found with ID: " + productId.getValue(), cause);
        this.identifier = productId.getValue();
        this.identifierType = "ID";
    }
    
    /**
     * Constructor for SKU lookup failure with cause
     */
    public ProductNotFoundException(SKU sku, Throwable cause) {
        super("Product not found with SKU: " + sku.getValue(), cause);
        this.identifier = sku.getValue();
        this.identifierType = "SKU";
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public String getIdentifierType() {
        return identifierType;
    }
}
