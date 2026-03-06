package com.mustapha.ecommerce.order.infrastructure.messaging;

import com.mustapha.ecommerce.cart.domain.event.CartConvertedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Cart Event Listener (Order Context)
 * 
 * Responsibility: React to Cart domain events for order tracking and analytics
 * Pattern: Event-Driven Architecture, Inter-Context Communication
 * Layer: INFRASTRUCTURE (Messaging)
 * 
 * Context Boundary: Cart → Order
 * - Cart context publishes CartConvertedEvent
 * - Order context listens and links cart to order for analytics
 * 
 * Design Decisions:
 * - @Async for non-blocking event processing
 * - Graceful error handling (logs errors but doesn't fail cart conversion)
 * 
 * Use Cases:
 * - Track conversion funnel (cart → order)
 * - Attribute orders to abandoned carts
 * - Calculate cart-to-order conversion rate
 * - Analyze time from cart creation to order placement
 * 
 * Production Considerations:
 * - Consider storing cart-order relationship in separate table
 * - Add metrics for conversion time analysis
 * - Enable attribution for marketing campaigns
 */
@Component
public class CartEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(CartEventListener.class);
    
    /**
     * When a cart is converted to an order, track the relationship
     * 
     * Business Logic:
     * - Link cartId to orderId for analytics
     * - Track conversion funnel completion
     * - Enable conversion rate calculation
     * 
     * Future Enhancements:
     * - Store cart-order relationship in database
     * - Calculate conversion time (cart created → order placed)
     * - Attribute order to marketing campaigns via cart
     * 
     * @param event CartConvertedEvent containing cart ID, order ID, amount, item count
     */
    @EventListener
    @Async
    public void onCartConverted(CartConvertedEvent event) {
        log.info("Received CartConvertedEvent - CartId: {}, OrderId: {}, TotalAmount: {}, ItemCount: {}",
            event.getCartId(),
            event.getOrderId(),
            event.getTotalAmount(),
            event.getItemCount());
        
        try {
            // TODO: Store cart-order relationship for analytics
            // Example: cartOrderRepository.save(new CartOrderLink(cartId, orderId, timestamp))
            
            // TODO: Calculate and track conversion time
            // Example: Cart createdAt → Order placedAt duration
            
            // TODO: Update conversion rate metrics
            // Example: metricsService.incrementConversionCount()
            
            log.info("Successfully processed cart conversion - Cart {} → Order {}",
                event.getCartId(),
                event.getOrderId());
                
        } catch (Exception e) {
            // Log error but don't fail - cart conversion already succeeded
            log.error("Failed to process cart conversion tracking for cart {} → order {}: {}",
                event.getCartId(),
                event.getOrderId(),
                e.getMessage(),
                e);
        }
    }
}
