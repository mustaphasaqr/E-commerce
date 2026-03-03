package com.mustapha.ecommerce.cart.application.usecase;

import com.mustapha.ecommerce.cart.application.command.ConvertCartCommand;
import com.mustapha.ecommerce.cart.domain.exception.CartNotFoundException;
import com.mustapha.ecommerce.cart.domain.model.Cart;
import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;
import com.mustapha.ecommerce.cart.domain.repository.CartRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Convert Cart Use Case
 * 
 * Responsibility: Mark cart as converted after order creation
 * Pattern: Use Case (Application Service)
 */
@Component
public class ConvertCartUseCase {
    
    private final CartRepository cartRepository;
    
    public ConvertCartUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    
    @Transactional
    public void execute(ConvertCartCommand command) {
        // Get cart by CartId value object
        Cart cart = cartRepository.findById(command.getCartId())
            .orElseThrow(() -> new CartNotFoundException(command.getCartId().getValue()));
        
        // Convert cart (domain logic)
        cart.convertToOrder(command.getOrderId());
        
        // Save
        cartRepository.save(cart);
    }
}
