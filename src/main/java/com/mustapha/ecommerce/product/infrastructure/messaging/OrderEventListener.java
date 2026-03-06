package com.mustapha.ecommerce.product.infrastructure.messaging;

import com.mustapha.ecommerce.order.domain.event.OrderCancelledEvent;
import com.mustapha.ecommerce.order.domain.event.OrderItemDto;
import com.mustapha.ecommerce.product.application.command.ReleaseReservationCommand;
import com.mustapha.ecommerce.product.application.usecase.ReleaseReservationUseCase;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Order Event Listener (Product Context)
 * 
 * Responsibility: React to Order domain events and release product stock
 * Pattern: Event-Driven Architecture, Inter-Context Communication
 * Layer: INFRASTRUCTURE (Messaging)
 * 
 * Context Boundary: Order → Product
 * - Order context publishes OrderCancelledEvent
 * - Product context listens and releases reserved stock
 * 
 * Design Decisions:
 * - @Async for non-blocking event processing
 * - Graceful error handling (logs errors but doesn't fail order cancellation)
 * - Idempotent (releaseReservation is idempotent in domain)
 * 
 * Production Considerations:
 * - Consider using message queue (RabbitMQ/Kafka) for reliability
 * - Add retry logic with exponential backoff
 * - Add dead-letter queue for failed stock releases
 * - Monitor stock release failures
 */
@Component
public class OrderEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    
    private final ReleaseReservationUseCase releaseReservationUseCase;
    
    public OrderEventListener(ReleaseReservationUseCase releaseReservationUseCase) {
        this.releaseReservationUseCase = releaseReservationUseCase;
    }
    
    /**
     * When an order is cancelled, release all reserved stock
     * 
     * Business Logic:
     * - Extract all product IDs and quantities from cancelled order
     * - For each product, release the stock reservation
     * - This makes stock available for other orders
     * 
     * Error Handling:
     * - If stock release fails for a product, log error but continue with others
     * - Stock release is idempotent, so safe to retry
     * 
     * @param event OrderCancelledEvent containing order ID, items, and reason
     */
    @EventListener
    @Async
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent for order: {}, releasing stock for {} products",
            event.orderId().getValue(),
            event.items().size());
        
        String orderId = event.orderId().getValue();
        
        for (OrderItemDto item : event.items()) {
            try {
                // Create command to release stock reservation
                ReleaseReservationCommand command = new ReleaseReservationCommand(
                    ProductId.of(item.productId()),
                    orderId
                );
                
                // Execute use case to release stock
                releaseReservationUseCase.execute(command);
                
                log.info("Successfully released {} units of product {} for cancelled order {}",
                    item.quantity(),
                    item.productId(),
                    orderId);
                
            } catch (Exception e) {
                // Log error but don't fail - order cancellation should succeed even if stock release fails
                // TODO: Add to dead-letter queue for manual reconciliation
                log.error("Failed to release stock for product {} in cancelled order {}: {}",
                    item.productId(),
                    orderId,
                    e.getMessage(),
                    e);
            }
        }
        
        log.info("Completed stock release processing for cancelled order: {}", orderId);
    }
}
