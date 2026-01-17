package com.mustapha.ecommerce.order.application.command;

import java.util.List;

import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;

/**
 * Place Order Command (Input DTO)
 * Responsibility: Transfer data from API layer to Application layer
 * Pattern: Command (CQS - Command Query Separation)
 * 
 * Note: Uses value objects (CustomerId, ProductId, Money) for type safety
 * The Facade converts primitives from API → value objects for this command
 */
public class PlaceOrderCommand {
    
    private final CustomerId customerId;
    private final List<OrderItemData> items;
    
    public PlaceOrderCommand(CustomerId customerId, List<OrderItemData> items) {
        this.customerId = customerId;
        this.items = items;
    }
    
    public CustomerId getCustomerId() {
        return customerId;
    }
    
    public List<OrderItemData> getItems() {
        return items;
    }
    
    /**
     * Order Item Data (nested DTO)
     * Uses value objects for type safety at application layer boundary
     */
    public static class OrderItemData {
        private final ProductId productId;
        private final String productName;
        private final int quantity;
        private final Money unitPrice;
        
        public OrderItemData(ProductId productId, String productName, int quantity, Money unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        public ProductId getProductId() {
            return productId;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public Money getUnitPrice() {
            return unitPrice;
        }
    }
}
