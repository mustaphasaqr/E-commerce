package com.mustapha.ecommerce.cart.application.usecase;

import com.mustapha.ecommerce.cart.application.command.ClearCartCommand;
import com.mustapha.ecommerce.cart.domain.exception.CartNotFoundException;
import com.mustapha.ecommerce.cart.domain.model.Cart;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.cart.domain.repository.CartRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clear Cart Use Case
 * 
 * Responsibility: Remove all items from cart
 * Pattern: Use Case (Application Service)
 */
@Component
public class ClearCartUseCase {
    
    private final CartRepository cartRepository;
    
    public ClearCartUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    
    @Transactional
    public Cart execute(ClearCartCommand command) {
        // Get cart
        Cart cart = findActiveCart(command.getUserId(), command.getSessionId());
        
        // Clear cart (domain logic)
        cart.clearCart();
        
        return cartRepository.save(cart);
    }
    
    private Cart findActiveCart(UserId userId, SessionId sessionId) {
        if (userId != null) {
            return cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("No active cart found for user " + userId.getValue()));
        }
        
        if (sessionId != null) {
            return cartRepository.findActiveCartBySessionId(sessionId)
                .orElseThrow(() -> new CartNotFoundException("No active cart found for session " + sessionId.getValue()));
        }
        
        throw new CartNotFoundException("No user ID or session ID provided");
    }
}
