package com.mustapha.ecommerce.cart.api.rest;

import com.mustapha.ecommerce.cart.api.dto.AddToCartRequest;
import com.mustapha.ecommerce.cart.api.dto.CartDTO;
import com.mustapha.ecommerce.cart.api.dto.UpdateCartItemRequest;
import com.mustapha.ecommerce.cart.application.facade.CartFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Cart REST Controller
 * API Layer - HTTP boundary for cart operations
 * 
 * Handles shopping cart operations for customers.
 * Supports both authenticated users and anonymous sessions.
 * 
 * Pattern: REST API Controller, delegates to CartFacade
 * SOLID: SRP (HTTP only), DIP (depends on facade interface)
 */
@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Shopping Cart", description = "Shopping cart management for authenticated users and anonymous sessions. Supports cart persistence, abandonment tracking, and seamless conversion from anonymous to authenticated carts.")
public class CartController {
    
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private static final java.util.regex.Pattern STRICT_NUMERIC_ID_PATTERN = java.util.regex.Pattern.compile("^(?:[A-Za-z_]+-)?(\\d+)$");
    
    private final CartFacade cartFacade;
    
    public CartController(CartFacade cartFacade) {
        this.cartFacade = cartFacade;
    }
    
    @Operation(
        summary = "Get Current Shopping Cart",
        description = """
            Retrieve the current shopping cart for authenticated user or anonymous session.
            
            **Features:**
            - Returns cart with all items and calculated totals
            - Creates empty cart if none exists
            - Supports both authenticated and anonymous users
            - Tracks cart abandonment (last_updated_at)
            - Persists across sessions for authenticated users
            
            **Security:** Public endpoint (works with or without authentication)
            """,
        tags = {"Shopping Cart"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cart retrieved successfully (or new empty cart created)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public ResponseEntity<CartDTO> getCart(
            @AuthenticationPrincipal String userIdStr,
            HttpSession session) {
        
        Long userId = parseUserId(userIdStr);
        String sessionId = session.getId();
        
        log.debug("Getting cart for userId={}, sessionId={}", userId, sessionId);
        
        CartDTO cart = cartFacade.getCart(userId, sessionId);
        return ResponseEntity.status(HttpStatus.OK).body(cart);
    }
    
    @Operation(
        summary = "Add Product to Cart",
        description = """
            Add a product to the shopping cart or increment quantity if already exists.
            
            **Features:**
            - Adds product to cart with specified quantity
            - Increments quantity if product already in cart
            - Validates product exists and is available
            - Automatically fetches product price and details
            - Recalculates cart total
            - Updates cart timestamp for abandonment tracking
            
            **Security:** Public endpoint (works with or without authentication)
            """,
        tags = {"Shopping Cart"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product added to cart successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid product ID or quantity",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found or unavailable",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/items")
    public ResponseEntity<CartDTO> addToCart(
            @Parameter(description = "Product ID and quantity to add", required = true)
            @Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal String userIdStr,
            HttpSession session) {
        
        Long userId = parseUserId(userIdStr);
        String sessionId = session.getId();
        
        log.info("Adding to cart: userId={}, sessionId={}, productId={}, quantity={}", 
                userId, sessionId, request.productId(), request.quantity());
        
        CartDTO cart = cartFacade.addToCart(request, userId, sessionId);
        
        return ResponseEntity.status(HttpStatus.OK).body(cart);
    }
    
    /**
     * Update cart item quantity
     * Example: PUT /api/cart/items
     * Body: { "productId": 123, "quantity": 5 }
     * (Set quantity to 0 to remove item)
     */
    @PutMapping("/items")
    @Operation(
        summary = "Update cart item quantity",
        description = "Updates the quantity of a product in the cart. " +
                     "Set quantity to 0 to remove the item from cart. " +
                     "Recalculates cart total and updates last_updated_at timestamp."
    )
    public ResponseEntity<CartDTO> updateCartItem(
            @Valid @RequestBody UpdateCartItemRequest request,
            @AuthenticationPrincipal String userIdStr,
            HttpSession session) {
        
        Long userId = parseUserId(userIdStr);
        String sessionId = session.getId();
        
        log.info("Updating cart item: userId={}, sessionId={}, productId={}, quantity={}", 
                userId, sessionId, request.productId(), request.quantity());
        
        CartDTO cart = cartFacade.updateCartItem(request, userId, sessionId);
        
        return ResponseEntity.status(HttpStatus.OK).body(cart);
    }
    
    /**
     * Remove item from cart
     * Example: DELETE /api/cart/items/{productId}
     */
    @DeleteMapping("/items/{productId}")
    @Operation(
        summary = "Remove item from cart",
        description = "Removes a product from the shopping cart by product ID. " +
                     "Recalculates cart total and updates last_updated_at timestamp."
    )
    public ResponseEntity<CartDTO> removeFromCart(
            @PathVariable String productId,
            @AuthenticationPrincipal String userIdStr,
            HttpSession session) {
        
        Long userId = parseUserId(userIdStr);
        String sessionId = session.getId();
        
        log.info("Removing from cart: userId={}, sessionId={}, productId={}", 
                userId, sessionId, productId);
        
        CartDTO cart = cartFacade.removeFromCart(productId, userId, sessionId);
        
        return ResponseEntity.status(HttpStatus.OK).body(cart);
    }
    
    @Operation(
        summary = "Clear Shopping Cart",
        description = """
            Remove all items from the cart. Cart remains active but empty.
            
            **Features:**
            - Removes all items from cart
            - Cart status remains ACTIVE
            - Cart total reset to 0
            - Updates last_updated_at timestamp
            
            **Security:** Public endpoint (works with or without authentication)
            """,
        tags = {"Shopping Cart"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cart cleared successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart not found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @DeleteMapping
    public ResponseEntity<CartDTO> clearCart(
            @AuthenticationPrincipal String userIdStr,
            HttpSession session) {
        
        Long userId = parseUserId(userIdStr);
        String sessionId = session.getId();
        
        log.info("Clearing cart: userId={}, sessionId={}", userId, sessionId);
        
        CartDTO cart = cartFacade.clearCart(userId, sessionId);
        
        return ResponseEntity.status(HttpStatus.OK).body(cart);
    }
    
    // Helper methods
    
    private Long parseUserId(String userIdStr) {
        if (userIdStr == null) {
            return null;
        }
        // Accept only numeric IDs or PREFIX-numeric format such as USER-123.
        java.util.regex.Matcher matcher = STRICT_NUMERIC_ID_PATTERN.matcher(userIdStr);
        if (!matcher.matches()) {
            return null;
        }
        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException ex) {
            log.warn("Unable to parse authentication principal '{}' into numeric user ID", userIdStr);
            return null;
        }
    }
}
