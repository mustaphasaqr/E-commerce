package com.mustapha.ecommerce.ecommerce.order.application.port;

import java.util.List;
import java.util.Map;

/**
 * Inventory Port - Hexagonal Architecture
 * Responsibility: Define contract for inventory operations
 */
public interface InventoryPort {
    boolean checkAvailability(List<Map<String, Object>> items);
    void reserve(List<Map<String, Object>> items);
    void release(List<Map<String, Object>> items);
}
