package com.mustapha.ecommerce.ecommerce.order.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mustapha.ecommerce.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.ecommerce.order.domain.service.PricingService;

/**
 * Order Builder
 * Pattern: Builder
 * Responsibility: Construct valid Order aggregates
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
        
        Order order = new Order();
        order.setId(new OrderId(UUID.randomUUID().toString()));
        order.setCustomerId(customerId);
        order.setItems(items);
        
        // Apply pricing rules
        if (pricingService != null) {
            Money finalPrice = pricingService.calculateFinalPrice(order);
            order.setTotalAmount(finalPrice);
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
