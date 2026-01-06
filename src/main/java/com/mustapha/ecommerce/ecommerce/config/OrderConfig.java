package com.mustapha.ecommerce.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mustapha.ecommerce.ecommerce.order.application.port.InventoryPort;
import com.mustapha.ecommerce.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.ecommerce.order.domain.repository.OrderRepository;
import com.mustapha.ecommerce.ecommerce.order.infrastructure.adapter.inventory.InventoryAdapter;
import com.mustapha.ecommerce.ecommerce.order.infrastructure.adapter.payment.PaymentAdapter;
import com.mustapha.ecommerce.ecommerce.order.infrastructure.persistence.repository.JpaOrderRepository;

/**
 * Order Configuration
 * Responsibility: Wire Order aggregate dependencies
 * Pattern: Composition Root
 */
@Configuration
public class OrderConfig {

    /**
     * Bind OrderRepository to JpaOrderRepository implementation
     */
    @Bean
    public OrderRepository orderRepository(JpaOrderRepository jpaOrderRepository) {
        return jpaOrderRepository;
    }

    /**
     * Bind PaymentPort to PaymentAdapter implementation
     */
    @Bean
    public PaymentPort paymentPort(PaymentAdapter paymentAdapter) {
        return paymentAdapter;
    }

    /**
     * Bind InventoryPort to InventoryAdapter implementation
     */
    @Bean
    public InventoryPort inventoryPort(InventoryAdapter inventoryAdapter) {
        return inventoryAdapter;
    }
}
