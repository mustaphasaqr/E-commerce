package com.mustapha.ecommerce.cart.application.usecase;

import com.mustapha.ecommerce.cart.application.command.AddToCartCommand;
import com.mustapha.ecommerce.cart.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.cart.application.port.ProductPort;
import com.mustapha.ecommerce.cart.domain.model.Cart;
import com.mustapha.ecommerce.cart.domain.model.valueobject.Money;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.cart.domain.repository.CartRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Add To Cart Use Case
 * 
 * Responsibility: Add product to cart or increment quantity
 * Pattern: Use Case (Application Service)
 */
@Component
public class AddToCartUseCase {
    
    private final CartRepository cartRepository;
    private final ProductPort productPort;
    private final DomainEventPublisher eventPublisher;
    
    public AddToCartUseCase(CartRepository cartRepository, ProductPort productPort, DomainEventPublisher eventPublisher) {
        this.cartRepository = cartRepository;
        this.productPort = productPort;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public Cart execute(AddToCartCommand command) {
        // Get or create cart
        Cart cart = getOrCreateCart(command.getUserId(), command.getSessionId());
        
        // Get product details
        String productName = productPort.getProductName(command.getProductId());
        Money price = productPort.getProductPrice(command.getProductId());
        
        // Add to cart (domain logic handles existing items)
        cart.addItem(command.getProductId(), productName, command.getQuantity(), price);
        
        return cartRepository.save(cart);
    }
    
    private Cart getOrCreateCart(UserId userId, SessionId sessionId) {
        // Try authenticated user cart first
        if (userId != null) {
            Optional<Cart> userCart = cartRepository.findActiveCartByUserId(userId);
            if (userCart.isPresent()) {
                return userCart.get();
            }
        }
        
        // Try session cart
        if (sessionId != null) {
            Optional<Cart> sessionCart = cartRepository.findActiveCartBySessionId(sessionId);
            if (sessionCart.isPresent()) {
                return sessionCart.get();
            }
        }
        
        // Create new cart using value objects
        return new Cart(userId, sessionId, eventPublisher);
    }
}
