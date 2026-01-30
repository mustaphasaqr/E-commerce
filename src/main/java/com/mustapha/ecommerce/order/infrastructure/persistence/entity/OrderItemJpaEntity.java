package com.mustapha.ecommerce.order.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.math.BigDecimal;

/**
 * Order Item JPA Entity
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_product", columnList = "product_id")
})
@Check(name = "chk_order_item_qty_positive", constraints = "quantity > 0")
@Check(name = "chk_order_item_price_positive", constraints = "price >= 0")
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    // Constructors
    public OrderItemJpaEntity() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
