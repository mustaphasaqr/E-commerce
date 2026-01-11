package com.mustapha.ecommerce.order.infrastructure.adapter.inventory;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.application.port.InventoryPort;
import com.mustapha.ecommerce.order.infrastructure.adapter.inventory.sdk.InventoryHttpClient;

import java.util.List;
import java.util.Map;

/**
 * Inventory Adapter
 * Responsibility: Implement InventoryPort using external inventory service
 * Pattern: Adapter (Hexagonal Architecture)
 */
@Component
public class InventoryAdapter implements InventoryPort {

    private final InventoryHttpClient inventoryHttpClient;

    public InventoryAdapter(InventoryHttpClient inventoryHttpClient) {
        this.inventoryHttpClient = inventoryHttpClient;
    }

    @Override
    public boolean checkAvailability(List<Map<String, Object>> items) {
        return inventoryHttpClient.checkStock(items);
    }

    @Override
    public void reserve(List<Map<String, Object>> items) {
        inventoryHttpClient.reserveStock(items);
    }

    @Override
    public void release(List<Map<String, Object>> items) {
        inventoryHttpClient.releaseStock(items);
    }
}
