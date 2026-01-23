package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.ReserveStockCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Reserve Stock for Order
 * 
 * Responsibility: Reserve product stock for specific order (cross-aggregate interaction)
 * 
 * Business Rules:
 * - Idempotent per orderId (handled by domain)
 * - Product must be active and available for purchase
 * - Emits StockUpdatedEvent on successful reservation
 * 
 * Pattern: Clean 3-Step Pattern (Write Operation)
 * Step 1: Load aggregate
 * Step 2: Execute domain method
 * Step 3: Save & publish events
 * 
 * Production-grade: OrderId tracking for traceability
 */
@Component
public class ReserveStockUseCase {
    
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;
    
    public ReserveStockUseCase(ProductRepository productRepository,
                               DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public Product execute(ReserveStockCommand command) {
        // Step 1: Load aggregate
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(command.getProductId()));
        
        // Step 2: Execute domain method (idempotent, validates business rules, emits event)
        product.reserveStockForOrder(command.getOrderId(), command.getQuantity());
        
        // Step 3: Save & publish events
        Product savedProduct = productRepository.save(product);
        savedProduct.getDomainEvents().forEach(eventPublisher::publish);
        savedProduct.clearDomainEvents();
        
        return savedProduct;
    }
}
