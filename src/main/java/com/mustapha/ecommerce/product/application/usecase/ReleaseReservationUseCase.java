package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.ReleaseReservationCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Release Stock Reservation
 * 
 * Responsibility: Cancel stock reservation for specific order
 * 
 * Business Rules:
 * - Idempotent (handled by domain)
 * - Restores available stock
 * - Emits StockUpdatedEvent if reservation existed
 * 
 * Pattern: Clean 3-Step Pattern (Write Operation)
 * Step 1: Load aggregate
 * Step 2: Execute domain method
 * Step 3: Save & publish events
 * 
 * Use Case: Order cancellation, payment failure, order timeout
 */
@Component
public class ReleaseReservationUseCase {
    
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;
    
    public ReleaseReservationUseCase(ProductRepository productRepository,
                                     DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public Product execute(ReleaseReservationCommand command) {
        // Step 1: Load aggregate
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(command.getProductId()));
        
        // Step 2: Execute domain method (idempotent, emits event if reservation existed)
        product.releaseReservationForOrder(command.getOrderId());
        
        // Step 3: Save & publish events
        Product savedProduct = productRepository.save(product);
        savedProduct.getDomainEvents().forEach(eventPublisher::publish);
        savedProduct.clearDomainEvents();
        
        return savedProduct;
    }
}
