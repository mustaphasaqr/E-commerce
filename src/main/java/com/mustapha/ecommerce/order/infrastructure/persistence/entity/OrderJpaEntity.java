package com.mustapha.ecommerce.order.infrastructure.persistence.entity;

import com.mustapha.ecommerce.shared.domain.AuditedEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mustapha.ecommerce.order.domain.model.OrderStatus;

/**
 * Order JPA Entity
 * Responsibility: Database mapping for Order aggregate
 * Optimistic locking with @Version to prevent race conditions
 * 
 * Audit Support:
 * - Extends AuditedEntity for created_by, created_at, updated_by, updated_at
 * - Critical for fraud prevention and customer disputes
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_customer", columnList = "customer_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created", columnList = "created_at")
})
@Check(name = "chk_order_total_positive", constraints = "total_amount >= 0")
public class OrderJpaEntity extends AuditedEntity {

    @Id
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // Shipping information - populated when order is shipped
    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "carrier")
    private String carrier;

    // Cancellation information - populated when order is cancelled
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    // Delivery information - populated when order is delivered
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    // Payment information - populated during payment flow
    @Column(name = "checkout_id", length = 2000)
    private String checkoutId;
    
    @Column(name = "transaction_id", length = 2000)
    private String transactionId;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    // Refund information - populated when order is refunded
    @Column(name = "refund_status")
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus = RefundStatus.NONE;
    
    @Column(name = "refund_amount", precision = 19, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "refund_date")
    private LocalDateTime refundDate;
    
    @Column(name = "refund_reason", length = 500)
    private String refundReason;
    
    // Geographic information - populated from shipping address
    @Column(name = "shipping_city", length = 100)
    private String shippingCity;
    
    @Column(name = "shipping_state", length = 100)
    private String shippingState;
    
    @Column(name = "shipping_country", length = 100)
    private String shippingCountry;
    
    @Column(name = "shipping_zip_code", length = 20)
    private String shippingZipCode;
    
    // Shipping timeline - populated when order is shipped
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    // Marketing attribution - populated at order creation
    @Column(name = "utm_source", length = 100)
    private String utmSource;
    
    @Column(name = "utm_campaign", length = 100)
    private String utmCampaign;
    
    @Column(name = "referrer", length = 500)
    private String referrer;

    /**
     * Optimistic locking version
     * Prevents race conditions in concurrent order updates (status changes, cancellations, shipments)
     */
    @Version
    private Long version;

    // Constructors
    public OrderJpaEntity() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemJpaEntity> getItems() {
        return items;
    }

    public void setItems(List<OrderItemJpaEntity> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(this::addItem);
        }
    }

    public void addItem(OrderItemJpaEntity item) {
        if (item == null) {
            return;
        }
        item.setOrder(this);
        this.items.add(item);
    }

    public void removeItem(OrderItemJpaEntity item) {
        if (item == null) {
            return;
        }
        this.items.remove(item);
        item.setOrder(null);
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public String getCheckoutId() {
        return checkoutId;
    }
    
    public void setCheckoutId(String checkoutId) {
        this.checkoutId = checkoutId;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public RefundStatus getRefundStatus() {
        return refundStatus;
    }
    
    public void setRefundStatus(RefundStatus refundStatus) {
        this.refundStatus = refundStatus;
    }
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public LocalDateTime getRefundDate() {
        return refundDate;
    }
    
    public void setRefundDate(LocalDateTime refundDate) {
        this.refundDate = refundDate;
    }
    
    public String getRefundReason() {
        return refundReason;
    }
    
    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }
    
    // Geographic getters and setters
    public String getShippingCity() {
        return shippingCity;
    }
    
    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }
    
    public String getShippingState() {
        return shippingState;
    }
    
    public void setShippingState(String shippingState) {
        this.shippingState = shippingState;
    }
    
    public String getShippingCountry() {
        return shippingCountry;
    }
    
    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }
    
    public String getShippingZipCode() {
        return shippingZipCode;
    }
    
    public void setShippingZipCode(String shippingZipCode) {
        this.shippingZipCode = shippingZipCode;
    }
    
    // Shipping timeline getters and setters
    public LocalDateTime getShippedAt() {
        return shippedAt;
    }
    
    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }
    
    // Marketing attribution getters and setters
    public String getUtmSource() {
        return utmSource;
    }
    
    public void setUtmSource(String utmSource) {
        this.utmSource = utmSource;
    }
    
    public String getUtmCampaign() {
        return utmCampaign;
    }
    
    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }
    
    public String getReferrer() {
        return referrer;
    }
    
    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
