package com.mustapha.ecommerce.cart.infrastructure.adapter;

import com.mustapha.ecommerce.cart.application.port.ProductPort;
import com.mustapha.ecommerce.cart.domain.model.valueobject.Money;
import com.mustapha.ecommerce.cart.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.application.facade.ProductFacade;
import com.mustapha.ecommerce.product.dto.ProductResponse;
import org.springframework.stereotype.Component;

/**
 * Product Adapter
 * Infrastructure Layer - Implements ProductPort using ProductFacade
 * 
 * Translates cart module needs into product module calls.
 * Converts between cart value objects and product module primitives.
 * 
 * Pattern: Adapter (Hexagonal Architecture)
 */
@Component
public class ProductAdapter implements ProductPort {
    
    private final ProductFacade productFacade;
    
    public ProductAdapter(ProductFacade productFacade) {
        this.productFacade = productFacade;
    }
    
    @Override
    public String getProductName(ProductId productId) {
        ProductResponse product = productFacade.getProductById(String.valueOf(productId.getValue()));
        return product.getName();
    }
    
    @Override
    public Money getProductPrice(ProductId productId) {
        ProductResponse product = productFacade.getProductById(String.valueOf(productId.getValue()));
        return new Money(product.getPrice());
    }
    
    @Override
    public boolean productExists(ProductId productId) {
        try {
            productFacade.getProductById(String.valueOf(productId.getValue()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
