package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.UpdateProductDetailsCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Update Product Details
 * 
 * Responsibility: Update product name and description
 * 
 * Business Rules:
 * - Name cannot be empty or exceed 200 characters (validated in domain)
 * - Description is optional
 * - Emits ProductDetailsUpdatedEvent
 * 
 * Pattern: Clean 3-Step Pattern (Write Operation)
 * Step 1: Load aggregate
 * Step 2: Execute domain method
 * Step 3: Save & publish events
 */
@Component
public class UpdateProductDetailsUseCase {
    
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;
    
    public UpdateProductDetailsUseCase(ProductRepository productRepository,
                                       DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public Product execute(UpdateProductDetailsCommand command) {
        // Step 1: Load aggregate
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(command.getProductId()));
        
        // Step 2: Execute domain method (validates name, emits event)
        product.updateDetails(command.getName(), command.getDescription());
        
        // Step 3: Save & publish events
        Product updatedProduct = productRepository.save(product);
        updatedProduct.getDomainEvents().forEach(eventPublisher::publish);
        updatedProduct.clearDomainEvents();
        
        return updatedProduct;
    }
}
