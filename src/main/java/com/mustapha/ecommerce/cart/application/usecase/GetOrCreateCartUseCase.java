package com.mustapha.ecommerce.cart.application.usecase;

import com.mustapha.ecommerce.cart.application.command.GetCartQuery;
import com.mustapha.ecommerce.cart.domain.model.Cart;
import com.mustapha.ecommerce.cart.domain.model.CartStatus;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.cart.domain.repository.CartRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Get Or Create Cart Use Case
 * 
 * Responsibility: Retrieve existing cart or create new one
 * Pattern: Use Case (Application Service)
 */
@Component
public class GetOrCreateCartUseCase {
    
    private final CartRepository cartRepository;
    
    public GetOrCreateCartUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    
    @Transactional
    public Cart execute(GetCartQuery query) {
        // Try authenticated user cart first
        if (query.getUserId() != null) {
            Optional<Cart> userCart = cartRepository.findActiveCartByUserId(query.getUserId());
            if (userCart.isPresent()) {
                return userCart.get();
            }
        }
        
        // Try session cart
        if (query.getSessionId() != null) {
            Optional<Cart> sessionCart = cartRepository.findActiveCartBySessionId(query.getSessionId());
            if (sessionCart.isPresent()) {
                return sessionCart.get();
            }
        }
        
        // Create new cart using value objects
        Cart newCart = new Cart(query.getUserId(), query.getSessionId());
        
        return cartRepository.save(newCart);
    }
}
