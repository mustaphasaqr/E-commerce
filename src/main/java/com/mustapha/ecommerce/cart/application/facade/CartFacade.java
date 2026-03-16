package com.mustapha.ecommerce.cart.application.facade;

import com.mustapha.ecommerce.cart.api.dto.AddToCartRequest;
import com.mustapha.ecommerce.cart.api.dto.CartDTO;
import com.mustapha.ecommerce.cart.api.dto.CartItemDTO;
import com.mustapha.ecommerce.cart.api.dto.UpdateCartItemRequest;
import com.mustapha.ecommerce.cart.application.command.*;
import com.mustapha.ecommerce.cart.application.usecase.*;
import com.mustapha.ecommerce.cart.domain.model.Cart;
import com.mustapha.ecommerce.cart.domain.model.CartItem;
import com.mustapha.ecommerce.cart.domain.model.valueobject.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Cart Facade - Translation Layer between API and Application
 * 
 * Responsibilities:
 * 1. Accept API DTOs (CartRequest with primitives)
 * 2. Convert primitives → commands
 * 3.Delegate to Use Cases (no business logic here)
 * 4. Convert Domain → API DTOs (CartDTO)
 * 
 * Pattern: Facade, Anti-Corruption Layer
 */
@Service
public class CartFacade {
    
    private final GetOrCreateCartUseCase getOrCreateCartUseCase;
    private final AddToCartUseCase addToCartUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;
    private final RemoveFromCartUseCase removeFromCartUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final ConvertCartUseCase convertCartUseCase;
    
    public CartFacade(GetOrCreateCartUseCase getOrCreateCartUseCase,
                     AddToCartUseCase addToCartUseCase,
                     UpdateCartItemUseCase updateCartItemUseCase,
                     RemoveFromCartUseCase removeFromCartUseCase,
                     ClearCartUseCase clearCartUseCase,
                     ConvertCartUseCase convertCartUseCase) {
        this.getOrCreateCartUseCase = getOrCreateCartUseCase;
        this.addToCartUseCase = addToCartUseCase;
        this.updateCartItemUseCase = updateCartItemUseCase;
        this.removeFromCartUseCase = removeFromCartUseCase;
        this.clearCartUseCase = clearCartUseCase;
        this.convertCartUseCase = convertCartUseCase;
    }
    
    /**
     * Get or create cart
     */
    public CartDTO getCart(Long userId, String sessionId) {
        GetCartQuery query = new GetCartQuery(
            new UserId(userId),
            new SessionId(sessionId)
        );
        Cart cart = getOrCreateCartUseCase.execute(query);
        return toDTO(cart);
    }
    
    /**
     * Add product to cart
     */
    public CartDTO addToCart(AddToCartRequest request, Long userId, String sessionId) {
        AddToCartCommand command = new AddToCartCommand(
            new UserId(userId),
            new SessionId(sessionId),
            new ProductId(request.productId()),
            request.quantity()
        );
        Cart cart = addToCartUseCase.execute(command);
        return toDTO(cart);
    }
    
    /**
     * Update cart item quantity
     */
    public CartDTO updateCartItem(UpdateCartItemRequest request, Long userId, String sessionId) {
        UpdateCartItemCommand command = new UpdateCartItemCommand(
            new UserId(userId),
            new SessionId(sessionId),
            new ProductId(request.productId()),
            request.quantity()
        );
        Cart cart = updateCartItemUseCase.execute(command);
        return toDTO(cart);
    }
    
    /**
     * Remove item from cart
     */
    public CartDTO removeFromCart(String productId, Long userId, String sessionId) {
        RemoveFromCartCommand command = new RemoveFromCartCommand(
            new UserId(userId),
            new SessionId(sessionId),
            new ProductId(productId)
        );
        Cart cart = removeFromCartUseCase.execute(command);
        return toDTO(cart);
    }
    
    /**
     * Clear cart
     */
    public CartDTO clearCart(Long userId, String sessionId) {
        ClearCartCommand command = new ClearCartCommand(
            new UserId(userId),
            new SessionId(sessionId)
        );
        Cart cart = clearCartUseCase.execute(command);
        return toDTO(cart);
    }
    
    /**
     * Convert cart to order (called after order creation)
     */
    public void convertCart(Long cartId, Long orderId) {
        if (cartId != null) {
            ConvertCartCommand command = new ConvertCartCommand(
                new CartId(cartId),
                orderId
            );
            convertCartUseCase.execute(command);
        }
    }
    
    // Conversion helpers
    
    private CartDTO toDTO(Cart cart) {
        return new CartDTO(
            cart.getId() != null ? cart.getId().getValue() : null,
            cart.getUserId() != null ? cart.getUserId().getValue() : null,
            cart.getSessionId() != null ? cart.getSessionId().getValue() : null,
            cart.getItems().stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList()),
            cart.getTotalAmount().getAmount(),
            cart.getStatus().name(),
            cart.getTotalItems()
        );
    }
    
    private CartItemDTO toItemDTO(CartItem item) {
        return new CartItemDTO(
            item.getProductId().getValue(),
            item.getProductName(),
            item.getQuantity(),
            item.getPrice().getAmount(),
            item.getSubtotal().getAmount()
        );
    }
}
