package com.mustapha.ecommerce.cart.application.usecase;

import com.mustapha.ecommerce.cart.application.command.UpdateCartItemCommand;
import com.mustapha.ecommerce.cart.domain.exception.CartNotFoundException;
import com.mustapha.ecommerce.cart.domain.model.Cart;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.cart.domain.repository.CartRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Update Cart Item Use Case
 * 
 * Responsibility: Update quantity of item in cart (or remove if quantity = 0)
 * Pattern: Use Case (Application Service)
 */
@Component
public class UpdateCartItemUseCase {
    
    private final CartRepository cartRepository;
    
    public UpdateCartItemUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    
    @Transactional
    public Cart execute(UpdateCartItemCommand command) {
        // Get cart
        Cart cart = findActiveCart(command.getUserId(), command.getSessionId());
        
        // Update quantity (domain logic handles removal if quantity = 0)
        if (command.getQuantity() == 0) {
            cart.removeItem(command.getProductId());
        } else {
            cart.updateItemQuantity(command.getProductId(), command.getQuantity());
        }
        
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
