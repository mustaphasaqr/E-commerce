package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.DiscontinueProductCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Discontinue Product Use Case
 * Responsibility: Mark product as discontinued - terminal state (NOT business rules)
 * Pattern: Use Case (Application Service)
 * 
 * Clean 3-Step Pattern:
 * 1. Load aggregate
 * 2. Execute domain method (discontinue - idempotent)
 * 3. Save & publish events
 * 
 * Advanced: Demonstrates irreversible state transitions (DDD pattern)
 */
@Component
public class DiscontinueProductUseCase {
    
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;

    public DiscontinueProductUseCase(ProductRepository productRepository,
                                    DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public Product execute(DiscontinueProductCommand command) {
        // Step 1: Load product aggregate
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(command.getProductId()));
        
        // Step 2: Execute domain method (idempotent, emits event if not already discontinued)
        product.discontinue();
        
        // Step 3: Save & publish events
        Product discontinuedProduct = productRepository.save(product);
        
        // Publish domain events
        discontinuedProduct.getDomainEvents().forEach(eventPublisher::publish);
        discontinuedProduct.clearDomainEvents();
        
        return discontinuedProduct;
    }
}
