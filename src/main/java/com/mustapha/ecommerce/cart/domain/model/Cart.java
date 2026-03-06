package com.mustapha.ecommerce.cart.domain.model;

import com.mustapha.ecommerce.cart.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.cart.domain.event.*;
import com.mustapha.ecommerce.cart.domain.model.valueobject.CartId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.Money;
import com.mustapha.ecommerce.cart.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.SessionId;
import com.mustapha.ecommerce.cart.domain.model.valueobject.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cart aggregate root representing a shopping cart.
 * 
 * Responsibilities:
 * - Manage cart items (add, update, remove, clear)
 * - Calculate total amount
 * - Track cart status (ACTIVE, CONVERTED, ABANDONED)
 * - Support both authenticated users and anonymous sessions
 * 
 * Domain Rules:
 * - Cart must have either userId OR sessionId (at least one non-null)
 * - Only ACTIVE carts can be modified
 * - Total amount is calculated from items (not set directly)
 * - Once converted or abandoned, cart cannot be modified
 */
public class Cart {
    
    private final DomainEventPublisher eventPublisher; // For publishing cart domain events
    
    private CartId id;
    private UserId userId; // For authenticated users (can be null)
    private SessionId sessionId; // For anonymous users (can be null)
    private List<CartItem> items;
    private Money totalAmount;
    private CartStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private Long convertedOrderId; // Reference to order context (primitive is OK)
    private Long version; // Optimistic locking version
    
    /**
     * Constructor for new carts
     * At least one of userId or sessionId must be provided
     */
    public Cart(UserId userId, SessionId sessionId, DomainEventPublisher eventPublisher) {
        validateCartOwnership(userId, sessionId);
        
        if (eventPublisher == null) {
            throw new IllegalArgumentException("Event publisher cannot be null");
        }
        
        this.eventPublisher = eventPublisher;
        this.userId = userId;
        this.sessionId = sessionId;
        this.items = new ArrayList<>();
        this.totalAmount = Money.ZERO;
        this.status = CartStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
        this.version = 0L;
    }
    
    /**
     * Constructor for loading existing carts from persistence
     */
    public Cart(CartId id, UserId userId, SessionId sessionId, List<CartItem> items, 
                Money totalAmount, CartStatus status, LocalDateTime createdAt, 
                LocalDateTime lastUpdatedAt, Long convertedOrderId, Long version,
                DomainEventPublisher eventPublisher) {
        validateCartOwnership(userId, sessionId);
        
        if (eventPublisher == null) {
            throw new IllegalArgumentException("Event publisher cannot be null");
        }
        
        this.eventPublisher = eventPublisher;
        this.id = id;
        this.userId = userId;
        this.sessionId = sessionId;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.totalAmount = totalAmount != null ? totalAmount : Money.ZERO;
        this.status = status;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
        this.convertedOrderId = convertedOrderId;
        this.version = version;
    }
    
    // ========== Domain Operations ==========
    
    /**
     * Add product to cart or increment quantity if already exists
     */
    public void addItem(ProductId productId, String productName, int quantity, Money price) {
        validateActive();
        
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        Optional<CartItem> existing = findItem(productId);
        if (existing.isPresent()) {
            existing.get().incrementQuantity(quantity);
        } else {
            items.add(new CartItem(productId, productName, quantity, price));
        }
        
        recalculateTotal();
        this.lastUpdatedAt = LocalDateTime.now();
        
        // Publish event for analytics and recommendations
        eventPublisher.publish(new CartItemAddedEvent(id, productId, productName, quantity, price));
    }
    
    /**
     * Update item quantity in cart
     */
    public void updateItemQuantity(ProductId productId, int quantity) {
        validateActive();
        
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        CartItem item = findItem(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not in cart: " + productId));
        
        item.setQuantity(quantity);
        recalculateTotal();
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    /**
     * Remove item from cart
     */
    public void removeItem(ProductId productId) {
        validateActive();
        
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        // Find item before removal to capture product name for event
        CartItem removed = findItem(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not in cart: " + productId));
        
        items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotal();
        this.lastUpdatedAt = LocalDateTime.now();
        
        // Publish event for analytics (abandonment tracking)
        eventPublisher.publish(new CartItemRemovedEvent(id, productId, removed.getProductName()));
    }
    
    /**
     * Clear all items from cart
     */
    public void clearCart() {
        validateActive();
        
        items.clear();
        totalAmount = Money.ZERO;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark cart as converted to order
     */
    public void convertToOrder(Long orderId) {
        validateActive();
        
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID: " + orderId);
        }
        
        this.status = CartStatus.CONVERTED;
        this.convertedOrderId = orderId;
        this.lastUpdatedAt = LocalDateTime.now();
        
        // Publish event for analytics (conversion tracking)
        eventPublisher.publish(new CartConvertedEvent(id, orderId, totalAmount, getTotalItems()));
    }
    
    /**
     * Mark cart as abandoned
     */
    public void markAsAbandoned() {
        if (this.status == CartStatus.ACTIVE) {
            this.status = CartStatus.ABANDONED;
            this.lastUpdatedAt = LocalDateTime.now();
            
            // Publish event for recovery emails and analytics
            eventPublisher.publish(new CartAbandonedEvent(id, totalAmount, getTotalItems(), userId));
        }
    }
    
    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    /**
     * Get total item count
     */
    public int getTotalItems() {
        return items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }
    
    /**
     * Check if cart has been idle for specified hours
     */
    public boolean isIdleFor(int hours) {
        return lastUpdatedAt.isBefore(LocalDateTime.now().minusHours(hours));
    }
    
    // ========== Private Helpers ==========
    
    private Optional<CartItem> findItem(ProductId productId) {
        return items.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst();
    }
    
    private void validateActive() {
        if (status != CartStatus.ACTIVE) {
            throw new IllegalStateException("Cannot modify cart with status: " + status);
        }
    }
    
    private void validateCartOwnership(UserId userId, SessionId sessionId) {
        if (userId == null && sessionId == null) {
            throw new IllegalArgumentException("Cart must have either userId or sessionId");
        }
    }
    
    private void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(CartItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }
    
    // ========== Getters and Setters ==========
    
    public CartId getId() {
        return id;
    }
    
    public void setId(CartId id) {
        this.id = id;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public SessionId getSessionId() {
        return sessionId;
    }
    
    public List<CartItem> getItems() {
        return new ArrayList<>(items); // Return defensive copy
    }
    
    public void setItems(List<CartItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        recalculateTotal();
    }
    
    public Money getTotalAmount() {
        return totalAmount;
    }
    
    public CartStatus getStatus() {
        return status;
    }
    
    public void setStatus(CartStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }
    
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
    
    public Long getConvertedOrderId() {
        return convertedOrderId;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
}
