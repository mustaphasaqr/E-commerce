package com.mustapha.ecommerce.order.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * Order Configuration
 * Responsibility: Wire Order aggregate dependencies
 * Pattern: Composition Root
 */
@Configuration
public class OrderConfig {

    // No explicit binding needed - Spring will auto-inject implementations
    // JpaOrderRepository implements OrderRepository
    // PaymentAdapter implements PaymentPort
    // InventoryAdapter implements InventoryPort
}
