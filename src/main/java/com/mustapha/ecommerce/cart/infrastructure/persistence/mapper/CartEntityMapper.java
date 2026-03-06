package com.mustapha.ecommerce.cart.infrastructure.persistence.mapper;

import com.mustapha.ecommerce.cart.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.cart.domain.model.Cart;
import com.mustapha.ecommerce.cart.domain.model.CartItem;
import com.mustapha.ecommerce.cart.domain.model.CartStatus;
import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.Money;
import com.mustapha.ecommerce.cart.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartJpaEntity;
import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartItemJpaEntity;
import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartStatusEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Cart Entity Mapper
 * Infrastructure Layer - Domain ↔ Persistence Translation
 * 
 * Responsibility: Convert between domain models and JPA entities
 * Pattern: Mapper / Adapter
 */
@Component
public class CartEntityMapper {
    
    private final DomainEventPublisher eventPublisher;
    
    public CartEntityMapper(DomainEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Convert domain model to JPA entity
     */
    public CartJpaEntity toEntity(Cart cart) {
        CartJpaEntity entity = new CartJpaEntity();
        
        if (cart.getId() != null) {
            entity.setId(cart.getId().getValue());
        }
        
        entity.setUserId(cart.getUserId() != null ? cart.getUserId().getValue() : null);
        entity.setSessionId(cart.getSessionId() != null ? cart.getSessionId().getValue() : null);
        entity.setTotalAmount(cart.getTotalAmount().getAmount());
        entity.setStatus(toEntityStatus(cart.getStatus()));
        entity.setCreatedAt(cart.getCreatedAt());
        entity.setLastUpdatedAt(cart.getLastUpdatedAt());
        entity.setConvertedOrderId(cart.getConvertedOrderId());
        entity.setVersion(cart.getVersion());
        
        // Map items
        for (CartItem item : cart.getItems()) {
            CartItemJpaEntity itemEntity = toItemEntity(item);
            entity.addItem(itemEntity);
        }
        
        return entity;
    }
    
    /**
     * Update existing entity with domain model data
     */
    public void updateEntity(CartJpaEntity entity, Cart cart) {
        entity.setUserId(cart.getUserId() != null ? cart.getUserId().getValue() : null);
        entity.setSessionId(cart.getSessionId() != null ? cart.getSessionId().getValue() : null);
        entity.setTotalAmount(cart.getTotalAmount().getAmount());
        entity.setStatus(toEntityStatus(cart.getStatus()));
        entity.setLastUpdatedAt(cart.getLastUpdatedAt());
        entity.setConvertedOrderId(cart.getConvertedOrderId());
        
        // Clear and rebuild items to handle adds/removes/updates
        entity.getItems().clear();
        for (CartItem item : cart.getItems()) {
            CartItemJpaEntity itemEntity = toItemEntity(item);
            entity.addItem(itemEntity);
        }
    }
    
    /**
     * Convert JPA entity to domain model
     */
    public Cart toDomain(CartJpaEntity entity) {
        return new Cart(
            entity.getId() != null ? new CartId(entity.getId()) : null,
            entity.getUserId() != null ? new UserId(entity.getUserId()) : null,
            entity.getSessionId() != null ? new SessionId(entity.getSessionId()) : null,
            entity.getItems().stream()
                .map(this::toItemDomain)
                .collect(Collectors.toList()),
            new Money(entity.getTotalAmount()),
            toDomainStatus(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getLastUpdatedAt(),
            entity.getConvertedOrderId(),
            entity.getVersion(),
            eventPublisher // Inject event publisher for loaded carts
        );
    }
    
    /**
     * Convert domain item to entity item
     */
    private CartItemJpaEntity toItemEntity(CartItem item) {
        CartItemJpaEntity entity = new CartItemJpaEntity();
        
        if (item.getId() != null) {
            entity.setId(item.getId());
        }
        
        entity.setProductId(item.getProductId().getValue());
        entity.setProductName(item.getProductName());
        entity.setQuantity(item.getQuantity());
        entity.setPrice(item.getPrice().getAmount());
        
        return entity;
    }
    
    /**
     * Convert entity item to domain item
     */
    private CartItem toItemDomain(CartItemJpaEntity entity) {
        return new CartItem(
            entity.getId(),
            new ProductId(entity.getProductId()),
            entity.getProductName(),
            entity.getQuantity(),
            new Money(entity.getPrice())
        );
    }
    
    /**
     * Convert domain status to entity status
     */
    private CartStatusEntity toEntityStatus(CartStatus status) {
        return CartStatusEntity.valueOf(status.name());
    }
    
    /**
     * Convert entity status to domain status
     */
    private CartStatus toDomainStatus(CartStatusEntity status) {
        return CartStatus.valueOf(status.name());
    }
}
