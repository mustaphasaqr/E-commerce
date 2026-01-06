package com.mustapha.ecommerce.ecommerce.order.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.mustapha.ecommerce.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.mustapha.ecommerce.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.mustapha.ecommerce.ecommerce.order.infrastructure.persistence.entity.OrderStatusEntity;

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
        entity.setCustomerId(order.getCustomerId());
        entity.setTotalAmount(order.getTotalAmount().getAmount());
        entity.setStatus(OrderStatusEntity.valueOf(order.getStatus().name()));
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());
        
        entity.setItems(order.getItems().stream()
                .map(this::toItemEntity)
                .collect(Collectors.toList()));
        
        return entity;
    }

    public Order toDomain(OrderJpaEntity entity) {
        Order order = new Order();
        order.setId(new OrderId(entity.getId()));
        order.setCustomerId(entity.getCustomerId());
        order.setTotalAmount(new Money(entity.getTotalAmount()));
        order.setStatus(OrderStatus.valueOf(entity.getStatus().name()));
        
        order.setItems(entity.getItems().stream()
                .map(this::toItemDomain)
                .collect(Collectors.toList()));
        
        return order;
    }

    private OrderItemJpaEntity toItemEntity(OrderItem item) {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.setProductId(item.getProductId());
        entity.setProductName(item.getProductName());
        entity.setQuantity(item.getQuantity());
        entity.setPrice(item.getPrice().getAmount());
        return entity;
    }

    private OrderItem toItemDomain(OrderItemJpaEntity entity) {
        return new OrderItem(
                entity.getProductId(),
                entity.getProductName(),
                entity.getQuantity(),
                new Money(entity.getPrice())
        );
    }
}
