package com.mustapha.ecommerce.cart.domain.repository;

import com.mustapha.ecommerce.cart.domain.model.Cart;
import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;

import java.util.Optional;

/**
 * Repository interface for Cart aggregate operations.
 * Follows hexagonal architecture - this is a port in the domain layer.
 */
public interface CartRepository {
    
    /**
     * Save or update a cart
     * @param cart Cart to save
     * @return Saved cart with generated ID
     */
    Cart save(Cart cart);
    
    /**
     * Find cart by ID
     * @param id Cart ID
     * @return Optional cart if found
     */
    Optional<Cart> findById(CartId id);
    
    /**
     * Find active cart for a user
     * @param userId User ID
     * @return Optional cart if found
     */
    Optional<Cart> findActiveCartByUserId(UserId userId);
    
    /**
     * Find active cart by session ID (for anonymous users)
     * @param sessionId Session identifier
     * @return Optional cart if found
     */
    Optional<Cart> findActiveCartBySessionId(SessionId sessionId);
    
    /**
     * Delete cart by ID
     * @param id Cart ID
     */
    void deleteById(CartId id);
    
    /**
     * Mark cart as converted to order
     * @param cartId Cart ID
     * @param orderId Order ID that was created from this cart
     */
    void markAsConverted(CartId cartId, Long orderId);
    
    /**
     * Mark cart as abandoned
     * @param cartId Cart ID
     */
    void markAsAbandoned(CartId cartId);
}
