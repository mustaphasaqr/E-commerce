package com.mustapha.ecommerce.order.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart JPA Entity
 * Tracks shopping carts for abandonment analysis
 * 
 * Business Rules:
 * - Carts older than 24 hours with no order are considered ABANDONED
 * - Carts converted to orders are marked as CONVERTED
 */
@Entity(name = "OrderCart")
@Table(name = "order_carts", indexes = {
    @Index(name = "idx_order_cart_user", columnList = "user_id"),
    @Index(name = "idx_order_cart_status", columnList = "status"),
    @Index(name = "idx_order_cart_created", columnList = "created_at"),
    @Index(name = "idx_order_cart_updated", columnList = "last_updated_at")
})
public class CartJpaEntity {

    @Id
    private String id;

    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "session_id")
    private String sessionId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private List<CartItemJpaEntity> items = new ArrayList<>();

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CartStatus status = CartStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;
    
    @Column(name = "converted_order_id")
    private String convertedOrderId;

    @Version
    private Long version;

    // Constructors
    public CartJpaEntity() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public CartStatus getStatus() {
        return status;
    }

    public void setStatus(CartStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
    
    public String getConvertedOrderId() {
        return convertedOrderId;
    }
    
    public void setConvertedOrderId(String convertedOrderId) {
        this.convertedOrderId = convertedOrderId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
