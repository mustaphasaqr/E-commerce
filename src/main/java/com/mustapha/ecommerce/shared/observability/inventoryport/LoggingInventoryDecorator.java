package com.mustapha.ecommerce.shared.observability.inventoryport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.InventoryPort;

import java.util.List;
import java.util.Map;

/**
 * Logging Inventory Decorator
 * Responsibility: Add logging to inventory operations
 * Pattern: Decorator
 */
@Component
public class LoggingInventoryDecorator implements InventoryPort {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInventoryDecorator.class);
    private final InventoryPort delegate;

    public LoggingInventoryDecorator(InventoryPort delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean checkAvailability(List<Map<String, Object>> items) {
        logger.info("Checking inventory availability for {} items", items.size());
        boolean available = delegate.checkAvailability(items);
        logger.info("Inventory availability check result: {}", available);
        return available;
    }

    @Override
    public void reserve(List<Map<String, Object>> items) {
        logger.info("Reserving inventory for {} items", items.size());
        delegate.reserve(items);
        logger.info("Inventory reserved successfully");
    }

    @Override
    public void release(List<Map<String, Object>> items) {
        logger.info("Releasing inventory for {} items", items.size());
        delegate.release(items);
        logger.info("Inventory released successfully");
    }
}
