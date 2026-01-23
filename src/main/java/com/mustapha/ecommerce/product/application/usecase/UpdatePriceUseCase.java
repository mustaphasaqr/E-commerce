package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.UpdatePriceCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

/**
 * Use Case: Update Product Price
 * 
 * Responsibility: Change product price with validation
 * 
 * Business Rules:
 * - Currency must match existing price currency (immutable)
 * - Price change limited to 10x increase / 90% decrease (typo protection)
 * - NO dependency on Order bounded context (reviewer approved)
 * 
 * Pattern: Clean 3-Step Pattern (Write Operation)
 * Step 1: Load aggregate
 * Step 2: Execute domain method
 * Step 3: Save & publish events
 * 
 * Note: Product aggregate handles all validation independently
 */
@Component
public class UpdatePriceUseCase {
    
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;
    
    public UpdatePriceUseCase(ProductRepository productRepository,
                              DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public Product execute(UpdatePriceCommand command) {
        // Step 1: Load aggregate
        Product product = productRepository.findById(command.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(command.getProductId()));
        
        // Step 2: Execute domain method (validates currency match, price change limits, emits event)
        product.updatePrice(command.getNewPrice());
        
        // Step 3: Save & publish events
        Product updatedProduct = productRepository.save(product);
        updatedProduct.getDomainEvents().forEach(eventPublisher::publish);
        updatedProduct.clearDomainEvents();
        
        return updatedProduct;
    }
}
