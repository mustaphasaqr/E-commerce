package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.GetProductBySkuQuery;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Use Case: Get Product By SKU
 * 
 * Responsibility: Retrieve product by external identifier (SKU)
 * 
 * Pattern: Clean 2-Step Pattern (Query Use Case)
 * Step 1: Convert primitive → value object
 * Step 2: Load & return aggregate
 * 
 * Note: Separate from GetProductById - different semantics (external vs internal ID)
 */
@Component
public class GetProductBySkuUseCase {
    
    private final ProductRepository productRepository;
    
    public GetProductBySkuUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public Product execute(GetProductBySkuQuery query) {
        // Step 1: Convert primitive → value object
        SKU sku = SKU.of(query.sku());
        
        // Step 2: Load & return aggregate
        return productRepository.findBySku(sku)
            .orElseThrow(() -> new ProductNotFoundException(sku));
    }
}
