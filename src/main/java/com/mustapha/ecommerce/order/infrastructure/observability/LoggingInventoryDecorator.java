package com.mustapha.ecommerce.order.infrastructure.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.InventoryPort;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;

/**
 * Logging Inventory Decorator
 * Responsibility: Add logging to inventory operations
 * Pattern: Decorator (Transparent logging wrapper)
 * 
 * Location: Order BC infrastructure (decorates Order's port to Product BC)
 */
@Component
public class LoggingInventoryDecorator implements InventoryPort {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInventoryDecorator.class);
    private final InventoryPort delegate;

    public LoggingInventoryDecorator(@Qualifier("inventoryAdapter") InventoryPort delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean checkAvailability(ProductId productId, int quantity) {
        logger.info("Checking inventory availability for product: {}, quantity: {}", productId.getValue(), quantity);
        boolean available = delegate.checkAvailability(productId, quantity);
        logger.info("Inventory availability check result: {}", available);
        return available;
    }

    @Override
    public void reserveStock(ProductId productId, int quantity) {
        logger.info("Reserving stock for product: {}, quantity: {}", productId.getValue(), quantity);
        delegate.reserveStock(productId, quantity);
        logger.info("Stock reserved successfully");
    }

    @Override
    public void releaseStock(ProductId productId, int quantity) {
        logger.info("Releasing stock for product: {}, quantity: {}", productId.getValue(), quantity);
        delegate.releaseStock(productId, quantity);
        logger.info("Stock released successfully");
    }

    @Override
    public void confirmReservation(ProductId productId, int quantity) {
        logger.info("Confirming reservation for product: {}, quantity: {}", productId.getValue(), quantity);
        delegate.confirmReservation(productId, quantity);
        logger.info("Reservation confirmed successfully");
    }
}
