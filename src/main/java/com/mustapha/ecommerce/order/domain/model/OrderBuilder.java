package com.mustapha.ecommerce.order.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.service.PricingService;

/**
 * Order Builder
 * Pattern: Builder
 * Responsibility: Construct valid Order aggregates with external dependencies (e.g., PricingService)
 * Usage: new OrderBuilder().withCustomerId("123").withItems(data).withPricingService(service).build()
 */
public class OrderBuilder {
    private String customerId;
    private List<OrderItem> items = new ArrayList<>();
    private PricingService pricingService;

    public OrderBuilder withCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public OrderBuilder withItems(List<Map<String, Object>> itemData) {
        for (Map<String, Object> data : itemData) {
            String productId = (String) data.get("productId");
            String productName = (String) data.get("productName");
            int quantity = (Integer) data.get("quantity");
            double price = (Double) data.get("price");
            
            this.items.add(new OrderItem(productId, productName, quantity, new Money(price)));
        }
        return this;
    }

    public OrderBuilder withPricingService(PricingService pricingService) {
        this.pricingService = pricingService;
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
        order.setItems(items);
        
        // Apply pricing rules if service provided
        if (pricingService != null) {
            Money finalPrice = pricingService.calculateFinalPrice(order);
            // Note: setTotalAmount removed - total is calculated automatically
            // If you need custom pricing, you'll need to add a package-private method
            // For now, total is auto-calculated from items
        }
        
        return order;
    }

    private void validateBuilder() {
        if (customerId == null || customerId.isEmpty()) {
            throw new IllegalStateException("Customer ID is required");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Order must have at least one item");
        }
    }
}
