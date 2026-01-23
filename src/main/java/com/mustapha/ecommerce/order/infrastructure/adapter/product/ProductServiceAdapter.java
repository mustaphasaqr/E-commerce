package com.mustapha.ecommerce.order.infrastructure.adapter.product;

import com.mustapha.ecommerce.order.application.exception.ProductNotFoundException;
import com.mustapha.ecommerce.order.application.port.ProductPort;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.application.facade.ProductFacade;
import com.mustapha.ecommerce.product.dto.ProductResponse;
import org.springframework.stereotype.Component;

/**
 * Product Service Adapter (Monolith Implementation)
 * Pattern: Adapter (Hexagonal Architecture), Anti-Corruption Layer
 * 
 * Implements ProductPort using direct calls to ProductFacade.
 * This is the MONOLITH implementation - no HTTP calls!
 * 
 * Responsibilities:
 * - Direct method calls to Product bounded context (same JVM)
 * - Value object translation (Order ProductId → Product ProductId)
 * - Exception translation (Product context → Order context)
 * - DTO conversion (ProductResponse → ProductInfo)
 * 
 * For microservices, swap this with ProductRestAdapter (HTTP calls).
 */
@Component
public class ProductServiceAdapter implements ProductPort {

    private final ProductFacade productFacade;
    
    public ProductServiceAdapter(ProductFacade productFacade) {
        this.productFacade = productFacade;
    }

    @Override
    public boolean productExists(ProductId productId) {
        try {
            // Translate Order ProductId → String (facade accepts primitives)
            productFacade.getProductById(productId.getValue());
            return true;
        } catch (com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException e) {
            return false;
        }
    }

    @Override
    public Money getProductPrice(ProductId productId) {
        try {
            // Translate Order ProductId → String (facade accepts primitives)
            ProductResponse response = productFacade.getProductById(productId.getValue());
            
            // Translate Product DTO → Order value object
            return new Money(response.getPrice());
        } catch (com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException e) {
            // Anti-corruption layer: Translate Product exception → Order exception
            throw new ProductNotFoundException(productId);
        }
    }

    @Override
    public ProductInfo getProductInfo(ProductId productId) {
        try {
            // Translate Order ProductId → String (facade accepts primitives)
            ProductResponse response = productFacade.getProductById(productId.getValue());
            
            // Translate Product DTO → Order ProductInfo
            return new ProductInfo(
                productId,
                response.getName(),
                response.getDescription(),
                new Money(response.getPrice()),
                response.isActive()
            );
        } catch (com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException e) {
            // Anti-corruption layer: Translate Product exception → Order exception
            throw new ProductNotFoundException(productId);
        }
    }
}
