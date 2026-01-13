package com.mustapha.ecommerce.order.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;

/**
 * Order Builder
 * Pattern: Builder
 * Responsibility: Construct valid Order aggregates
 * 
 * Phase 1: Builds Order with items only. Total = Σ items (invariant preserved).
 * Pricing/discounts handled in Application layer, NOT in domain aggregate.
 * 
 * Usage: new OrderBuilder().withCustomerId("123").withItems(data).build()
 */
public class OrderBuilder {
    private CustomerId customerId;
    private List<OrderItem> items = new ArrayList<>();

    public OrderBuilder withCustomerId(String customerId) {
        this.customerId = new CustomerId(customerId);
        return this;
    }

    public OrderBuilder withItems(List<Map<String, Object>> itemData) {
        for (Map<String, Object> data : itemData) {
            String productId = (String) data.get("productId");
            String productName = (String) data.get("productName");
            int quantity = (Integer) data.get("quantity");
            double price = (Double) data.get("price");
            
            this.items.add(new OrderItem(new ProductId(productId), productName, quantity, new Money(price)));
        }
        return this;
    }

    public Order build() {
        validateBuilder();
        
        // Create order using package-private constructor
        Order order = new Order();
        order.setId(new OrderId(UUID.randomUUID().toString()));
        
        // Set customer ID (package-private setter)
        order.setCustomerId(customerId);
        
        // Set items (package-private setter) - triggers recalculateTotal()
        // Order invariant: total = Σ items (no discounts at this layer)
        order.setItems(items);
        
        return order;
    }

    private void validateBuilder() {
        if (customerId == null) {
            throw new IllegalStateException("Customer ID is required");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Order must have at least one item");
        }
    }
}
