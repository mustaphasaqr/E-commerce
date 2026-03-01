package com.mustapha.ecommerce.order.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mustapha.ecommerce.order.domain.DomainEvent;
import com.mustapha.ecommerce.order.domain.event.OrderPlacedEvent;
import com.mustapha.ecommerce.order.domain.exception.InvalidOrderItemException;
import com.mustapha.ecommerce.order.domain.exception.InvalidOrderStateException;
import com.mustapha.ecommerce.order.domain.exception.OrderModificationNotAllowedException;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

/**
 * Order Aggregate Root
 * Responsibility: Business rules, Invariants enforcement, Domain Events
 * Pattern: Aggregate Root, Builder, Domain Events
 * SOLID: SRP, OCP, LSP
 */
public class Order {
    private static final int MAX_TOTAL_QUANTITY = 100; // Business rule: Max items in one order
    
    private OrderId id;
    private CustomerId customerId;
    private List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;
    // Removed: boolean isPaid - payment state is now part of OrderStatus (PAID)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Shipping information - populated when order is shipped
    private String trackingNumber;
    private String carrier;
    
    // Cancellation information - populated when order is cancelled
    private String cancellationReason;
    
    // Delivery information - populated when order is delivered
    private LocalDateTime deliveredAt;
    
    // Payment information - populated during payment flow
    private String checkoutId; // Accept (Paymob) payment key / checkout session ID
    private String transactionId; // Accept transaction ID after successful payment
    
    // Optimistic locking version (infrastructure concern tracked in domain for persistence)
    private Long version;
    
    // Domain Events - uncommitted events to be published
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Package-private constructor - accessible by OrderBuilder in same package
    Order() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Factory method for reconstituting Order from persistence (JPA/database)
     * Used by OrderMapper to restore orders with their original state
     * Bypasses business validation since historical data might not match current rules
     * 
     * @param id Original OrderId from database
     * @param customerId CustomerId from database
     * @param items List of OrderItems from database
     * @param status OrderStatus from database
     * @param createdAt Creation timestamp
     * @param updatedAt Last update timestamp
     * @param trackingNumber Shipping tracking number (null if not shipped)
     * @param carrier Shipping carrier (null if not shipped)
     * @param deliveredAt Delivery timestamp (null if not delivered)
     * @param cancellationReason Cancellation reason (null if not cancelled)
     * @param checkoutId Payment checkout ID (null if payment not initiated)
     * @param transactionId Payment transaction ID (null if payment not completed)
     * @return Reconstituted Order with all original state
     */
    public static Order reconstitute(
            OrderId id,
            CustomerId customerId,
            List<OrderItem> items,
            OrderStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String trackingNumber,
            String carrier,
            LocalDateTime deliveredAt,
            String cancellationReason,
            String checkoutId,
            String transactionId,
            Long version) {
        Order order = new Order();
        order.id = id;
        order.customerId = customerId;
        order.items = new ArrayList<>(items);
        order.status = status;
        order.createdAt = createdAt;
        order.updatedAt = updatedAt;
        order.trackingNumber = trackingNumber;
        order.carrier = carrier;
        order.deliveredAt = deliveredAt;
        order.cancellationReason = cancellationReason;
        order.checkoutId = checkoutId;
        order.transactionId = transactionId;
        order.version = version;
        order.recalculateTotal(); // Ensure total matches loaded items
        return order;
    }

    // ========== Business Rules: Order Lifecycle ==========
    
    /**
     * Rule: Cannot confirm if no items
     * Rule: Order must have items and total > 0
     * Status: PENDING → CONFIRMED
     * Raises: OrderPlacedEvent
     */
    public void confirm() {
        // Validate transition using rich enum behavior
        if (!this.status.canTransitionTo(OrderStatus.CONFIRMED)) {
            throw new InvalidOrderStateException("Cannot transition to CONFIRMED from status: " + this.status);
        }
        if (this.items == null || this.items.isEmpty()) {
            throw new InvalidOrderStateException("Cannot confirm order with no items");
        }
        if (this.totalAmount == null || this.totalAmount.getAmount() <= 0) {
            throw new InvalidOrderStateException("Cannot confirm order with total amount <= 0");
        }
        
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
        
        // Raise domain event
        this.raiseEvent(new OrderPlacedEvent(this.id, this.customerId, this.totalAmount));
    }
    
    /**
     * Rule: Cannot pay twice
     * Rule: Can only pay confirmed orders
     * Status: CONFIRMED → PAID
     */
    public void markAsPaid() {
        // Use rich enum behavior
        if (this.status.isPaid()) {
            throw new InvalidOrderStateException("Cannot pay twice - order already paid");
        }
        if (!this.status.canTransitionTo(OrderStatus.PAID)) {
            throw new InvalidOrderStateException("Cannot transition to PAID from status: " + this.status);
        }
        
        this.status = OrderStatus.PAID;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Rule: Cannot process if not paid
     * Status: PAID → PROCESSING
     */
    public void startProcessing() {
        if (!this.status.canTransitionTo(OrderStatus.PROCESSING)) {
            throw new InvalidOrderStateException("Cannot transition to PROCESSING from status: " + this.status);
        }
        
        this.status = OrderStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Rule: Cannot ship if not processing
     * Payment is guaranteed by status (PROCESSING means already PAID)
     * Status: PROCESSING → SHIPPED
     */
    public void ship(String trackingNumber, String carrier) {
        if (!this.status.canTransitionTo(OrderStatus.SHIPPED)) {
            throw new InvalidOrderStateException("Cannot transition to SHIPPED from status: " + this.status);
        }
        
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Rule: Cannot deliver if not shipped
     * Status: SHIPPED → DELIVERED
     */
    public void deliver(LocalDateTime deliveredAt) {
        if (!this.status.canTransitionTo(OrderStatus.DELIVERED)) {
            throw new InvalidOrderStateException("Cannot transition to DELIVERED from status: " + this.status);
        }
        
        this.deliveredAt = deliveredAt;
        this.status = OrderStatus.DELIVERED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Rule: Cannot cancel if shipped/delivered
     * Status: Any → CANCELLED
     */
    public void cancel(String reason) {
        if (!this.status.isCancellable()) {
            throw new InvalidOrderStateException("Cannot cancel order in status: " + this.status);
        }
        
        this.cancellationReason = reason;
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========== Business Rules: Order Content ==========
    
    /**
     * Rule: Cannot modify after confirmation
     * Rule: Item quantity > 0 and price > 0 (enforced by OrderItem)
     * Rule: Max total quantity in order
     */
    public void addItem(OrderItem item) {
        validateItemCanBeAdded(item);
        
        // Rule: Max quantity per order
        int currentTotalQuantity = items.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
        
        if (currentTotalQuantity + item.getQuantity() > MAX_TOTAL_QUANTITY) {
            throw new InvalidOrderItemException(
                "Cannot add item - would exceed max order quantity of " + MAX_TOTAL_QUANTITY
            );
        }
        
        this.items.add(item);
        recalculateTotal();
    }
    
    /**
     * Rule: Cannot modify items after confirmation
     */
    public void removeItem(OrderItem item) {
        if (!this.status.isModifiable()) {
            throw new OrderModificationNotAllowedException("Cannot remove items - order is not modifiable. Current status: " + this.status);
        }
        
        this.items.remove(item);
        recalculateTotal();
    }

    // ========== Domain Invariants ==========
    
    private void validateItemCanBeAdded(OrderItem item) {
        // Rule: Item cannot be null
        if (item == null) {
            throw new InvalidOrderItemException("Order item cannot be null");
        }
        
        // Rule: Cannot modify after confirmation - use rich enum behavior
        if (!this.status.isModifiable()) {
            throw new OrderModificationNotAllowedException("Cannot add items - order is not modifiable. Current status: " + this.status);
        }
        
        // OrderItem itself enforces: quantity > 0 and price > 0
    }

    /**
     * Recalculate order total from items
     * Uses Money arithmetic to preserve BigDecimal precision
     */
    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getTotal)
                .reduce(new Money(0), Money::add);
    }

    // ========== Getters (No dangerous setters!) ==========
    
    public OrderId getId() {
        return id;
    }

    // Package-private for JPA/ORM only - NOT for business logic
    void setId(OrderId id) {
        this.id = id;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    // Package-private - can only be set during order creation
    void setCustomerId(CustomerId customerId) {
        this.customerId = customerId;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    // Rule: Cannot modify after confirmation - validation enforced
    void setItems(List<OrderItem> items) {
        if (!this.status.isModifiable()) {
            throw new OrderModificationNotAllowedException("Cannot modify items - order is not modifiable. Current status: " + this.status);
        }
        
        this.items = items;
        recalculateTotal();
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    // NO setter - total is ALWAYS calculated, never set directly
    // Use addItem/removeItem to change total

    public OrderStatus getStatus() {
        return status;
    }

    // NO setter - status changes ONLY through behavior methods:
    // confirm(), markAsPaid(), startProcessing(), ship(), deliver(), cancel()

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public String getCarrier() {
        return carrier;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    public String getCheckoutId() {
        return checkoutId;
    }
    
    public void setCheckoutId(String checkoutId) {
        this.checkoutId = checkoutId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Long getVersion() {
        return version;
    }
    
    /**
     * Convenience method: Check if order is paid
     * Delegates to OrderStatus enum behavior
     */
    public boolean isPaid() {
        return this.status.isPaid();
    }
    
    // ========== Domain Events ==========
    
    /**
     * Raise a domain event (not published yet, just recorded)
     * Events will be published by infrastructure after aggregate is saved
     */
    private void raiseEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    /**
     * Get all uncommitted domain events
     * Called by repository after saving aggregate
     */
    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }
    
    /**
     * Clear domain events after they've been published
     * Called by repository after publishing events
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}