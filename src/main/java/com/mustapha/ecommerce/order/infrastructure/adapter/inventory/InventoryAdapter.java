package com.mustapha.ecommerce.order.infrastructure.adapter.inventory;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.InventoryPort;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.infrastructure.adapter.inventory.sdk.InventoryHttpClient;

/**
 * Inventory Adapter
 * Responsibility: Implement InventoryPort using external inventory service
 * Pattern: Adapter (Hexagonal Architecture)
 * 
 * NOTE: Stub implementation for development
 * Production: Connect to real inventory/warehouse management system
 */
@Component
@Primary
public class InventoryAdapter implements InventoryPort {

    private final InventoryHttpClient inventoryHttpClient;

    public InventoryAdapter(InventoryHttpClient inventoryHttpClient) {
        this.inventoryHttpClient = inventoryHttpClient;
    }

    @Override
    public boolean checkAvailability(ProductId productId, int quantity) {
        // Delegate to inventory HTTP client
        return inventoryHttpClient.checkStock(productId.getValue(), quantity);
    }

    @Override
    public void reserveStock(ProductId productId, int quantity) {
        // Reserve stock temporarily for order
        inventoryHttpClient.reserveStock(productId.getValue(), quantity);
    }

    @Override
    public void releaseStock(ProductId productId, int quantity) {
        // Release reserved stock (order cancelled)
        inventoryHttpClient.releaseStock(productId.getValue(), quantity);
    }

    @Override
    public void confirmReservation(ProductId productId, int quantity) {
        // Confirm reservation (order paid) - finalizes stock deduction
        inventoryHttpClient.confirmReservation(productId.getValue(), quantity);
    }
}
