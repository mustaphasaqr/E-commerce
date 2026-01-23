package com.mustapha.ecommerce.order.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.product.domain.event.*;

/**
 * Product Event Listener (Monolith Implementation)
 * Pattern: Event Listener
 * 
 * Listens to Product domain events using Spring @EventListener (in-memory).
 * Order bounded context reacts to Product changes without direct coupling.
 * 
 * For microservices, swap @EventListener to @KafkaListener or @RabbitListener.
 */
@Component
public class ProductEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductEventListener.class);
    
    /**
     * React to product creation
     * 
     * Business logic: Update product catalog cache or notification systems
     */
    @EventListener
    public void onProductCreated(ProductCreatedEvent event) {
        logger.info("Product created - Product: {}, SKU: {}, Name: {}", 
            event.productId(),
            event.sku(),
            event.name());
        
        // TODO: Implement business logic if needed
        // - Update product catalog cache
        // - Notify interested customers about new products
    }
    
    /**
     * React to product price updates
     * 
     * Business logic: Recalculate pending order totals if price changed
     */
    @EventListener
    public void onProductPriceUpdated(PriceChangedEvent event) {
        logger.info("Product price updated - Product: {}, Old: {}, New: {}", 
            event.productId(),
            event.oldPrice(),
            event.newPrice());
        
        // TODO: Implement business logic
        // - Find pending orders with this product
        // - Decide: Keep old price for existing orders? Notify customer?
        // - Update order totals if needed
    }
    
    /**
     * React to product details updates
     * 
     * Business logic: Update product information in pending orders
     */
    @EventListener
    public void onProductDetailsUpdated(ProductDetailsUpdatedEvent event) {
        logger.info("Product details updated - Product: {}, Name: {}", 
            event.productId(),
            event.newName());
        
        // TODO: Implement business logic if needed
        // - Update product name/description in pending orders
        // - Refresh product catalog cache
    }
    
    /**
     * React to product discontinuation
     * 
     * Business logic: Cancel pending orders or notify customers
     */
    @EventListener
    public void onProductDiscontinued(ProductDiscontinuedEvent event) {
        logger.warn("Product discontinued - Product: {}", event.productId());
        
        // TODO: Implement business logic
        // - Find pending orders with discontinued product
        // - Cancel orders or notify customers
        // - Prevent new orders for this product
    }
    
    /**
     * React to stock updates
     * 
     * Business logic: Notify customers if back in stock
     */
    @EventListener
    public void onStockUpdated(StockUpdatedEvent event) {
        int availableStock = event.newTotal() - event.newReserved();
        logger.debug("Stock updated - Product: {}, Total: {}, Reserved: {}, Available: {}", 
            event.productId(),
            event.newTotal(),
            event.newReserved(),
            availableStock);
        
        // TODO: Implement business logic
        // - If product was out of stock and now available
        // - Notify customers waiting for this product
    }
    
    /**
     * React to product activation
     */
    @EventListener
    public void onProductActivated(ProductActivatedEvent event) {
        logger.info("Product activated - Product: {}", event.productId());
        
        // TODO: Implement business logic if needed
    }
    
    /**
     * React to product deactivation
     */
    @EventListener
    public void onProductDeactivated(ProductDeactivatedEvent event) {
        logger.info("Product deactivated - Product: {}", event.productId());
        
        // TODO: Implement business logic
        // - Prevent new orders for inactive products
    }
}
