package com.mustapha.ecommerce.cart.infrastructure.persistence.entity;

import com.mustapha.ecommerce.shared.domain.AuditedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart JPA Entity
 * Infrastructure Layer - Persistence Model
 * 
 * Tracks shopping carts for abandonment analysis and order conversion
 * 
 * Audit Support:
 * - Extends AuditedEntity for created_by, created_at, updated_by, updated_at
 * - Tracks cart modifications for analytics
 */
@Entity(name = "Cart")
@Table(name = "carts", indexes = {
    @Index(name = "idx_cart_user", columnList = "user_id"),
    @Index(name = "idx_cart_session", columnList = "session_id"),
    @Index(name = "idx_cart_status", columnList = "status"),
    @Index(name = "idx_cart_created", columnList = "created_at"),
    @Index(name = "idx_cart_updated", columnList = "last_updated_at"),
    @Index(name = "idx_cart_converted_order", columnList = "converted_order_id")
})
public class CartJpaEntity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "session_id", length = 255)
    private String sessionId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemJpaEntity> items = new ArrayList<>();

    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CartStatusEntity status = CartStatusEntity.ACTIVE;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;
    
    @Column(name = "converted_order_id")
    private Long convertedOrderId;

    @Version
    private Long version;

    // Constructors
    public CartJpaEntity() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<CartItemJpaEntity> getItems() {
        return items;
    }

    public void setItems(List<CartItemJpaEntity> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public CartStatusEntity getStatus() {
        return status;
    }

    public void setStatus(CartStatusEntity status) {
        this.status = status;
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

    public void setConvertedOrderId(Long convertedOrderId) {
        this.convertedOrderId = convertedOrderId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
    
    // Helper methods
    public void addItem(CartItemJpaEntity item) {
        items.add(item);
        item.setCart(this);
    }
    
    public void removeItem(CartItemJpaEntity item) {
        items.remove(item);
        item.setCart(null);
    }
}
