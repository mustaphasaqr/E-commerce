package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.GetProductByIdQuery;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;

import org.springframework.stereotype.Component;

/**
 * Get Product By ID Use Case
 * Responsibility: Retrieve product by internal identifier (NOT business rules)
 * Pattern: Query Use Case (read-only, no @Transactional)
 * 
 * Clean 2-Step Pattern:
 * 1. Convert primitive → value object
 * 2. Load & return aggregate
 */
@Component
public class GetProductByIdUseCase {
    
    private final ProductRepository productRepository;
    
    public GetProductByIdUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public Product execute(GetProductByIdQuery query) {
        return productRepository.findById(query.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(query.getProductId()));
    }
}
