package com.mustapha.ecommerce.product.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Product JPA Entity
 * Pattern: JPA Entity (Infrastructure Layer)
 * 
 * Persistence model for Product aggregate
 * Optimistic locking with @Version
 * 
 * Performance Optimization:
 * - Indexes on active, discontinued for filtering queries
 * - Unique index on SKU for fast lookups
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku", unique = true),
    @Index(name = "idx_product_active", columnList = "active"),
    @Index(name = "idx_product_discontinued", columnList = "discontinued")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_product_sku", columnNames = "sku")
})
@Check(name = "chk_product_price_positive", constraints = "price >= 0")
@Check(name = "chk_product_stock_positive", constraints = "total_stock >= 0 AND available_stock >= 0 AND reserved_stock >= 0")
public class ProductJpaEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;
    
    /**
     * Product images (stored in AWS S3 or local storage)
     * List of image URLs
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", 
                     joinColumns = @JoinColumn(name = "product_id"),
                     foreignKey = @ForeignKey(name = "fk_product_image"))
    @Column(name = "image_url", length = 500)
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private int totalStock;

    @Column(nullable = false)
    private int availableStock;

    @Column(nullable = false)
    private int reservedStock;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean visible;

    @Column(nullable = false)
    private boolean availableForPurchase;

    @Column(nullable = false)
    private boolean discontinued;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Stock reservations (orderId -> quantity)
     * Stored as JSON in database
     * Note: Check constraint on quantity > 0 enforced at application layer and production DB schema
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_reservations", 
                     joinColumns = @JoinColumn(name = "product_id"),
                     foreignKey = @ForeignKey(name = "fk_reservation_product"))
    @MapKeyColumn(name = "order_id")
    @Column(name = "quantity", nullable = false)
    private Map<String, Integer> reservations = new HashMap<>();

    /**
     * Optimistic locking
     */
    @Version
    private Long version;

    // Constructors
    public ProductJpaEntity() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public int getReservedStock() {
        return reservedStock;
    }

    public void setReservedStock(int reservedStock) {
        this.reservedStock = reservedStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isAvailableForPurchase() {
        return availableForPurchase;
    }

    public void setAvailableForPurchase(boolean availableForPurchase) {
        this.availableForPurchase = availableForPurchase;
    }

    public boolean isDiscontinued() {
        return discontinued;
    }

    public void setDiscontinued(boolean discontinued) {
        this.discontinued = discontinued;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Integer> getReservations() {
        return reservations;
    }

    public void setReservations(Map<String, Integer> reservations) {
        this.reservations = reservations;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
