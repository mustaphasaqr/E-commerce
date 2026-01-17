package com.mustapha.ecommerce.order.application.port;

import com.mustapha.ecommerce.order.application.exception.InsufficientStockException;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;

/**
 * Inventory Port (Outbound Port)
 * Responsibility: Manage product stock/inventory operations
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation will be in Infrastructure layer
 * Examples: InventoryService, WarehouseAPI, StockManagementSystem
 */
public interface InventoryPort {
    
    /**
     * Check if product has sufficient stock
     * 
     * @param productId The product to check
     * @param quantity Required quantity
     * @return true if stock available, false otherwise
     */
    boolean checkAvailability(ProductId productId, int quantity);
    
    /**
     * Reserve stock for an order (temporary hold)
     * Call this when order is placed but not yet confirmed
     * 
     * @param productId The product to reserve
     * @param quantity Quantity to reserve
     * @throws InsufficientStockException if not enough stock
     */
    void reserveStock(ProductId productId, int quantity);
    
    /**
     * Release reserved stock (cancel reservation)
     * Call this when order is cancelled or reservation expires
     * 
     * @param productId The product to release
     * @param quantity Quantity to release
     */
    void releaseStock(ProductId productId, int quantity);
    
    /**
     * Confirm stock reservation (finalize deduction)
     * Call this when order is confirmed/paid
     * Converts reservation into actual stock deduction
     * 
     * @param productId The product to confirm
     * @param quantity Quantity to confirm
     */
    void confirmReservation(ProductId productId, int quantity);
}
