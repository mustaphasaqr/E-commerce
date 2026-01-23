package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.DeactivateProductCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deactivate Product Use Case
 * Responsibility: Make product inactive (NOT business rules)
 * Pattern: Use Case (Application Service)
 * 
 * Clean 3-Step Pattern:
 * 1. Load aggregate
 * 2. Execute domain method (deactivate)
 * 3. Save & publish events
 */
@Component
public class DeactivateProductUseCase {
    
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;

    public DeactivateProductUseCase(ProductRepository productRepository,
                                   DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public Product execute(DeactivateProductCommand command) {
        // Step 1: Load product aggregate
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(command.getProductId()));
        
        // Step 2: Execute domain method (validates no reserved stock, emits event)
        product.deactivate();
        
        // Step 3: Save & publish events
        Product deactivatedProduct = productRepository.save(product);
        
        // Publish domain events
        deactivatedProduct.getDomainEvents().forEach(eventPublisher::publish);
        deactivatedProduct.clearDomainEvents();
        
        return deactivatedProduct;
    }
}
