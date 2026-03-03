package com.mustapha.ecommerce.cart.infrastructure.persistence.repository;

import com.mustapha.ecommerce.cart.domain.model.Cart;
import com.mustapha.ecommerce.cart.domain.model.CartStatus;
import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.cart.domain.repository.CartRepository;
import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartJpaEntity;
import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartStatusEntity;
import com.mustapha.ecommerce.cart.infrastructure.persistence.mapper.CartEntityMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * JPA CartRepository Adapter
 * Infrastructure Layer - Implements Domain Repository Port
 * 
 * Uses Spring Data JPA repository and mapper to persist domain models.
 * 
 * Pattern: Repository, Adapter (Hexagonal Architecture)
 */
@Repository
public class JpaCartRepository implements CartRepository {
    
    private final SpringDataCartRepository springDataRepository;
    private final CartEntityMapper mapper;
    
    public JpaCartRepository(SpringDataCartRepository springDataRepository, CartEntityMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }
    
    @Override
    @Transactional
    public Cart save(Cart cart) {
        CartJpaEntity entity;
        
        if (cart.getId() == null) {
            // New cart
            entity = mapper.toEntity(cart);
        } else {
            // Update existing cart
            entity = springDataRepository.findById(cart.getId().getValue())
                .orElseGet(() -> mapper.toEntity(cart));
            mapper.updateEntity(entity, cart);
        }
        
        CartJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Cart> findById(CartId id) {
        return springDataRepository.findById(id.getValue())
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Cart> findActiveCartByUserId(UserId userId) {
        return springDataRepository.findByUserIdAndStatus(userId.getValue(), CartStatusEntity.ACTIVE)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Cart> findActiveCartBySessionId(SessionId sessionId) {
        return springDataRepository.findBySessionIdAndStatus(sessionId.getValue(), CartStatusEntity.ACTIVE)
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional
    public void deleteById(CartId id) {
        springDataRepository.deleteById(id.getValue());
    }
    
    @Override
    @Transactional
    public void markAsConverted(CartId cartId, Long orderId) {
        springDataRepository.findById(cartId.getValue()).ifPresent(entity -> {
            entity.setStatus(CartStatusEntity.CONVERTED);
            entity.setConvertedOrderId(orderId);
            springDataRepository.save(entity);
        });
    }
    
    @Override
    @Transactional
    public void markAsAbandoned(CartId cartId) {
        springDataRepository.findById(cartId.getValue()).ifPresent(entity -> {
            if (entity.getStatus() == CartStatusEntity.ACTIVE) {
                entity.setStatus(CartStatusEntity.ABANDONED);
                springDataRepository.save(entity);
            }
        });
    }
}
