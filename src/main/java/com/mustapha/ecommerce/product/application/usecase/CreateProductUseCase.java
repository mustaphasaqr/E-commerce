package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.CreateProductCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Create Product Use Case
 * Responsibility: Orchestrate product creation (NOT business rules)
 * Pattern: Use Case (Application Service)
 * 
 * Clean 3-Step Pattern:
 * 1. Validate SKU uniqueness (application concern)
 * 2. Create aggregate (Product.create factory)
 * 3. Save & publish events
 */
@Component
public class CreateProductUseCase {
    
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateProductUseCase(ProductRepository productRepository,
                               DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public Product execute(CreateProductCommand command) {
        // Step 1: Validate SKU uniqueness (application-level concern)
        if (productRepository.existsBySku(command.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + command.getSku().getValue() + " already exists");
        }
        
        // Step 2: Create product aggregate (emits ProductCreatedEvent)
        Product product = Product.create(
            command.getSku(),
            command.getName(),
            command.getDescription(),
            command.getPrice(),
            command.getStock()
        );
        
        // Step 3: Save & publish events
        Product savedProduct = productRepository.save(product);
        
        // Publish domain events
        savedProduct.getDomainEvents().forEach(eventPublisher::publish);
        savedProduct.clearDomainEvents();
        
        return savedProduct;
    }
}
