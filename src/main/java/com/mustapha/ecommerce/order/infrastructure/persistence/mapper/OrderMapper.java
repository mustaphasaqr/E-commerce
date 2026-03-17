package com.mustapha.ecommerce.order.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;

import java.util.stream.Collectors;

/**
 * Order Mapper
 * Responsibility: Map between domain and persistence models
 */
@Component
public class OrderMapper {

    public OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId().getValue());
        entity.setCustomerId(order.getCustomerId().getValue()); // Convert CustomerId → String
        entity.setTotalAmount(order.getTotalAmount().getAmountAsBigDecimal()); // Convert Money → BigDecimal
        entity.setStatus(order.getStatus()); // Use domain enum directly
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());
        entity.setTrackingNumber(order.getTrackingNumber());
        entity.setCarrier(order.getCarrier());
        entity.setDeliveredAt(order.getDeliveredAt());
        entity.setCancellationReason(order.getCancellationReason());
        entity.setPaymentMethod(order.getPaymentMethod());
        entity.setCheckoutId(order.getCheckoutId());
        entity.setTransactionId(order.getTransactionId());
        entity.setVersion(order.getVersion()); // Preserve version for optimistic locking
        
        order.getItems().stream()
            .map(this::toItemEntity)
            .forEach(entity::addItem);
        
        return entity;
    }

    public Order toDomain(OrderJpaEntity entity) {
        // Convert JPA entity items to domain OrderItems
        var items = entity.getItems().stream()
                .map(this::toItemDomain)
                .collect(Collectors.toList());
        
        // Use reconstitute to restore order with original state from database
        // This preserves ID, status, and timestamps (unlike OrderBuilder which creates NEW orders)
        return Order.reconstitute(
                new OrderId(entity.getId()),
                new CustomerId(entity.getCustomerId()),
                items,
                entity.getStatus(), // Use domain enum directly
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getTrackingNumber(),
                entity.getCarrier(),
                entity.getDeliveredAt(),
                entity.getCancellationReason(),
                entity.getPaymentMethod(),
                entity.getCheckoutId(),
                entity.getTransactionId(),
                entity.getVersion() // Pass version for optimistic locking
        );
    }

    private OrderItemJpaEntity toItemEntity(OrderItem item) {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.setProductId(item.getProductId().getValue()); // Convert ProductId → String
        entity.setProductName(item.getProductName());
        entity.setQuantity(item.getQuantity());
        entity.setPrice(item.getPrice().getAmountAsBigDecimal()); // Convert Money → BigDecimal
        return entity;
    }

    private OrderItem toItemDomain(OrderItemJpaEntity entity) {
        return new OrderItem(
                new ProductId(entity.getProductId()), // Convert String → ProductId
                entity.getProductName(),
                entity.getQuantity(),
                new Money(entity.getPrice())
        );
    }
}
