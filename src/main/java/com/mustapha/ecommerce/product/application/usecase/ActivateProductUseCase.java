package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.ActivateProductCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Activate Product Use Case
 * Responsibility: Make product active and available for purchase (NOT business rules)
 * Pattern: Use Case (Application Service)
 * 
 * Clean 3-Step Pattern:
 * 1. Load aggregate
 * 2. Execute domain method (activate)
 * 3. Save & publish events
 */
@Component
public class ActivateProductUseCase {
    
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;

    public ActivateProductUseCase(ProductRepository productRepository,
                                  DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public Product execute(ActivateProductCommand command) {
        // Step 1: Load product aggregate
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(command.getProductId()));
        
        // Step 2: Execute domain method (validates state, emits event)
        product.activate();
        
        // Step 3: Save & publish events
        Product activatedProduct = productRepository.save(product);
        
        // Publish domain events
        activatedProduct.getDomainEvents().forEach(eventPublisher::publish);
        activatedProduct.clearDomainEvents();
        
        return activatedProduct;
    }
}
