package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.FulfillReservationCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fulfill Reservation Use Case
 * Responsibility: Complete stock reservation when order ships (NOT business rules)
 * Pattern: Use Case (Application Service)
 * 
 * Clean 3-Step Pattern:
 * 1. Load aggregate
 * 2. Execute domain method (fulfillReservation - NOT idempotent)
 * 3. Save & publish events
 * 
 * Use Case: Order shipment, order completion
 */
@Component
public class FulfillReservationUseCase {
    
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;

    public FulfillReservationUseCase(ProductRepository productRepository,
                                    DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public Product execute(FulfillReservationCommand command) {
        // Step 1: Load product aggregate
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(command.getProductId()));
        
        // Step 2: Execute domain method (throws if reservation doesn't exist, emits event)
        product.fulfillReservationForOrder(command.getOrderId());
        
        // Step 3: Save & publish events
        Product savedProduct = productRepository.save(product);
        
        // Publish domain events
        savedProduct.getDomainEvents().forEach(eventPublisher::publish);
        savedProduct.clearDomainEvents();
        
        return savedProduct;
    }
}
