package com.mustapha.ecommerce.product.domain.model;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.event.*;
import com.mustapha.ecommerce.product.domain.exception.*;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product Aggregate Root
 * Responsibility: Product business logic with invariants protection
 * Pattern: DDD Aggregate Root
 * 
 * Invariants:
 * - Product ID and SKU cannot change once created
 * - Product name cannot be empty or exceed 200 characters  
 * - Price must be >= 0.01 with valid currency
 * - Price currency is immutable
 * - Total stock >= reserved stock (always)
 * - Available stock = total - reserved (always >= 0)
 * - Cannot reserve stock from inactive or discontinued product
 * - Cannot activate already active product
 * - Cannot deactivate already inactive product
 * - Discontinued is terminal state (cannot reactivate)
 * - Cannot modify discontinued product
 * - Visible controls display, availableForPurchase controls ordering
 */
public class Product {
    private final ProductId id;
    private final SKU sku;
    private String name;
    private String description;
    private List<String> imageUrls; // Product images stored in AWS S3 or local storage
    private Price price;
    private Stock stock;
    
    // State flags
    private boolean active;
    private boolean visible;
    private boolean availableForPurchase;
    private boolean discontinued;
    
    // Version control for optimistic locking
    @Version
    private int version;
    
    // Temporal
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private final List<ProductDomainEvent> domainEvents = new ArrayList<>();

    // Private constructor for invariants protection
    private Product(ProductId id, SKU sku, String name, String description, 
                   List<String> imageUrls, Price price, Stock stock, boolean active, boolean visible,
                   boolean availableForPurchase, boolean discontinued, int version,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
        this.price = price;
        this.stock = stock;
        this.active = active;
        this.visible = visible;
        this.availableForPurchase = availableForPurchase;
        this.discontinued = discontinued;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory method: Create new product
     */
    public static Product create(SKU sku, String name, String description, Price price, Stock stock) {
        validateProductName(name);
        
        Product product = new Product(
            ProductId.generate(),
            sku,
            name,
            description,
            new ArrayList<>(), // empty image list initially
            price,
            stock,
            true,          // active by default
            true,          // visible by default
            true,          // available for purchase by default
            false,         // not discontinued
            1,             // version starts at 1
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        product.domainEvents.add(new ProductCreatedEvent(product.id, sku.getValue(), name));
        return product;
    }

    /**
     * Factory method: Reconstitute from database
     */
    public static Product reconstitute(ProductId id, SKU sku, String name, String description,
                                      List<String> imageUrls, Price price, Stock stock, boolean active, boolean visible,
                                      boolean availableForPurchase, boolean discontinued, int version,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Product(id, sku, name, description, imageUrls, price, stock, active, visible,
                          availableForPurchase, discontinued, version, createdAt, updatedAt);
    }

    /**
     * Reserve stock for specific order (e.g., for order processing)
     * Business Rule: Product must be active, available for purchase, and not discontinued
     * Business Rule: Same OrderId cannot reserve twice (idempotent)
     */
    public void reserveStockForOrder(String orderId, int quantity) {
        // Guard: Cannot modify discontinued product
        ensureNotDiscontinued();
        
        // Guard: Product must be active
        if (!active) {
            throw new InvalidProductStateException(
                "Cannot reserve stock for inactive product: " + id.getValue()
            );
        }
        
        // Guard: Product must be available for purchase
        if (!availableForPurchase) {
            throw new InvalidProductStateException(
                "Cannot reserve stock for product not available for purchase: " + id.getValue()
            );
        }
        
        // Guard: Quantity must be positive
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be positive");
        }
        
        // Execute state change (idempotent - Stock handles duplicate OrderId)
        int previousQuantity = stock.getQuantity();
        int previousReserved = stock.getReservedQuantity();
        try {
            this.stock = stock.reserveForOrder(orderId, quantity);
        } catch (IllegalArgumentException e) {
            // Translate stock-level exception to domain exception with product context
            if (e.getMessage().contains("Insufficient available stock")) {
                throw new InsufficientStockException(
                    id.getValue(),
                    stock.getAvailableQuantity(),
                    quantity
                );
            }
            throw e; // Re-throw other IllegalArgumentExceptions
        }
        
        // Only update if reservation actually happened (not idempotent return)
        if (this.stock.getReservedQuantity() != previousReserved) {
            this.updatedAt = LocalDateTime.now();
            incrementVersion();
            
            // Raise domain event AFTER successful state change
            domainEvents.add(new StockUpdatedEvent(id, 
                previousQuantity, stock.getQuantity(),
                previousReserved, stock.getReservedQuantity()));
            assertInvariants();
        }
    }
    
    /**
     * Release reservation for specific order (e.g., order cancelled)
     */
    public void releaseReservationForOrder(String orderId) {
        // Guard: Cannot modify discontinued product
        ensureNotDiscontinued();
        
        // Execute state change (idempotent - Stock handles missing OrderId)
        int previousQuantity = stock.getQuantity();
        int previousReserved = stock.getReservedQuantity();
        this.stock = stock.releaseReservationForOrder(orderId);
        
        // Only update if reservation was actually released
        if (this.stock.getReservedQuantity() != previousReserved) {
            this.updatedAt = LocalDateTime.now();
            incrementVersion();
            
            // Raise domain event AFTER successful state change (total unchanged, only reserved decreased)
            domainEvents.add(new StockUpdatedEvent(id, 
                previousQuantity, stock.getQuantity(),
                previousReserved, stock.getReservedQuantity()));
            assertInvariants();
        }
    }
    
    /**
     * Fulfill reservation for specific order (e.g., order shipped)
     */
    public void fulfillReservationForOrder(String orderId) {
        // Guard: Cannot modify discontinued product
        ensureNotDiscontinued();
        
        // Execute state change
        int previousQuantity = stock.getQuantity();
        int previousReserved = stock.getReservedQuantity();
        this.stock = stock.fulfillReservationForOrder(orderId);
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        // Raise domain event AFTER successful state change
        domainEvents.add(new StockUpdatedEvent(id, 
            previousQuantity, stock.getQuantity(),
            previousReserved, stock.getReservedQuantity()));
        assertInvariants();
    }

    /**
     * Update product price
     * Business Rule: Price changes are tracked via events, currency must match
     * Business Rule: Cannot update price if product is in active orders (application layer checks)
     * Business Rule: Price change limited to prevent accidental typos (max 10x increase/decrease)
     */
    public void updatePrice(Price newPrice, boolean hasActiveOrders) {
        // Guard: Cannot modify discontinued product
        ensureNotDiscontinued();
        
        // Guard: Cannot update price while product is in active orders
        if (hasActiveOrders) {
            throw new ProductInUseException(
                "Cannot update price for product " + id.getValue() + 
                " - it is referenced in active orders. Price changes only affect future orders."
            );
        }
        
        // Guard: Price cannot be null
        if (newPrice == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        
        // Guard: Currency must match (immutable)
        this.price.ensureSameCurrency(newPrice);
        
        // Guard: Price must actually change
        if (this.price.equals(newPrice)) {
            return; // No-op if price hasn't changed
        }
        
        // Guard: Prevent drastic price changes (typo protection)
        // Allow max 10x increase or 90% decrease to catch mistakes
        java.math.BigDecimal percentageChange = this.price.calculatePercentageChange(newPrice);
        // percentageChange: 1.0 = 100% increase (2x), -0.5 = 50% decrease
        // Convert to ratio: ratio = 1 + percentageChange
        // Example: 100% increase → 1.0 + 1.0 = 2.0x, 50% decrease → 1.0 + (-0.5) = 0.5x
        java.math.BigDecimal ratio = java.math.BigDecimal.ONE.add(percentageChange);
        
        if (ratio.compareTo(java.math.BigDecimal.TEN) > 0 || 
            ratio.compareTo(new java.math.BigDecimal("0.1")) < 0) {
            throw new IllegalArgumentException(
                "Price change too drastic (" + String.format("%.1f", percentageChange.multiply(new java.math.BigDecimal("100"))) + "%). " +
                "Old: " + this.price.getAmount() + ", New: " + newPrice.getAmount() + ". " +
                "Maximum allowed: 10x increase or 90% decrease. Use discontinue + create if intentional."
            );
        }
        
        // Execute state change
        Price oldPrice = this.price;
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        // Raise domain event AFTER successful state change
        domainEvents.add(new PriceChangedEvent(id, oldPrice, newPrice));
    }
    
    /**
     * Update product price (legacy method - assumes no active orders)
     * Warning: Does not check for active orders - prefer updatePrice(Price, boolean)
     */
    public void updatePrice(Price newPrice) {
        updatePrice(newPrice, false);
    }

    /**
     * Update product details (name and description)
     * Business Rule: Cannot modify discontinued product
     * Business Rule: Cannot update details if product is in active orders (optional - based on business requirements)
     */
    public void updateDetails(String name, String description, boolean hasActiveOrders) {
        // Guard: Cannot modify discontinued product
        ensureNotDiscontinued();
        
        // Guard: Cannot update critical details while product is in active orders
        if (hasActiveOrders) {
            throw new ProductInUseException(
                "Cannot update details for product " + id.getValue() + 
                " - it is referenced in active orders. Changes only affect future orders."
            );
        }
        
        validateProductName(name);
        
        this.name = name;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        // Raise domain event AFTER successful state change
        domainEvents.add(new ProductDetailsUpdatedEvent(id, name, description));
    }
    
    /**
     * Update product details (legacy method - assumes no active orders)
     */
    public void updateDetails(String name, String description) {
        updateDetails(name, description, false);
    }

    /**
     * Deactivate product
     * Business Rule: Cannot deactivate already inactive product
     * Business Rule: Cannot deactivate product with reserved stock (active reservations exist)
     */
    public void deactivate() {
        // Guard: Cannot modify discontinued product
        ensureNotDiscontinued();
        
        // Guard: Cannot deactivate if already inactive
        if (!active) {
            throw new ProductAlreadyInactiveException(id.getValue());
        }
        
        // Guard: Cannot deactivate with reserved stock (would leave orders in inconsistent state)
        if (stock.getReservedQuantity() > 0) {
            throw new InvalidProductStateException(
                "Cannot deactivate product " + id.getValue() + 
                " - it has " + stock.getReservedQuantity() + " units reserved in active orders. " +
                "Release or fulfill reservations first."
            );
        }
        
        // Execute state change
        this.active = false;
        this.availableForPurchase = false; // Cannot purchase inactive product
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        // Raise domain event AFTER successful state change
        domainEvents.add(new ProductDeactivatedEvent(id));
    }

    /**
     * Activate product
     * Business Rule: Cannot activate already active product or discontinued product
     */
    public void activate() {
        // Guard: Cannot activate discontinued product (terminal state)
        if (discontinued) {
            throw new ProductDiscontinuedException(id.getValue());
        }
        
        // Guard: Cannot activate if already active
        if (active) {
            throw new ProductAlreadyActiveException(id.getValue());
        }
        
        // Execute state change
        this.active = true;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        // Raise domain event AFTER successful state change
        domainEvents.add(new ProductActivatedEvent(id));
    }
    
    /**
     * Discontinue product (terminal state - cannot be reversed)
     * Business Rule: Discontinued products cannot be modified or reactivated
     */
    public void discontinue() {
        if (discontinued) {
            return; // Already discontinued
        }
        
        // When discontinued, mark as inactive and unavailable
        this.discontinued = true;
        this.active = false;
        this.availableForPurchase = false;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        // Raise domain event AFTER successful state change
        domainEvents.add(new ProductDiscontinuedEvent(id));
    }
    
    /**
     * Hide product from catalog (still exists, just not shown)
     * 
     * Note: State-only change in MVP. Event will be added when
     * asynchronous consumers (search index, catalog cache) exist.
     */
    public void hide() {
        ensureNotDiscontinued();
        
        if (!visible) {
            return; // Already hidden
        }
        
        this.visible = false;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
    }
    
    /**
     * Show product in catalog
     * 
     * Note: State-only change in MVP. Event will be added when
     * asynchronous consumers (search index, catalog cache) exist.
     */
    public void show() {
        ensureNotDiscontinued();
        
        if (visible) {
            return; // Already visible
        }
        
        this.visible = true;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
    }
    
    /**
     * Make product available for purchase (can be visible but not purchasable)
     * 
     * Note: State-only change in MVP. Event will be added when
     * asynchronous consumers (inventory UI, recommendation engine) exist.
     */
    public void makeAvailableForPurchase() {
        ensureNotDiscontinued();
        
        if (availableForPurchase) {
            return;
        }
        
        this.availableForPurchase = true;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
    }
    
    /**
     * Make product unavailable for purchase (can be visible but not orderable)
     * 
     * Note: State-only change in MVP. Event will be added when
     * asynchronous consumers (inventory UI, recommendation engine) exist.
     */
    public void makeUnavailableForPurchase() {
        ensureNotDiscontinued();
        
        if (!availableForPurchase) {
            return;
        }
        
        this.availableForPurchase = false;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
    }
    
    /**
     * Check if stock is available for requested quantity
     */
    public boolean isStockAvailable(int quantity) {
        return active && availableForPurchase && !discontinued && stock.hasQuantity(quantity);
    }
    
    // Validation
    
    private static void validateProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("Product name cannot exceed 200 characters");
        }
    }
    
    // Private helper methods
    
    private void ensureNotDiscontinued() {
        if (discontinued) {
            throw new ProductDiscontinuedException(id.getValue());
        }
    }
    
    /**
     * Assert all aggregate invariants are maintained
     * Called after state changes to detect invariant violations early
     * 
     * Critical invariants:
     * - Stock total >= reserved (always)
     * - Reserved >= 0, Total >= 0
     */
    private void assertInvariants() {
        // Invariant: Stock total must be >= reserved
        if (stock.getQuantity() < stock.getReservedQuantity()) {
            throw new IllegalStateException(
                "Invariant violated: Total stock (" + stock.getQuantity() + 
                ") cannot be less than reserved (" + stock.getReservedQuantity() + ")"
            );
        }
        
        // Invariant: Stock quantities must be non-negative
        if (stock.getQuantity() < 0 || stock.getReservedQuantity() < 0) {
            throw new IllegalStateException(
                "Invariant violated: Stock quantities cannot be negative. " +
                "Total: " + stock.getQuantity() + ", Reserved: " + stock.getReservedQuantity()
            );
        }
    }
    
    private void incrementVersion() {
        this.version++;
    }
    
    /**
     * Add product image
     * Business Rule: Cannot add duplicate image URLs
     * Business Rule: Maximum 10 images per product
     */
    public void addImage(String imageUrl) {
        ensureNotDiscontinued();
        
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL cannot be empty");
        }
        
        if (imageUrls.contains(imageUrl)) {
            // Idempotent - already exists
            return;
        }
        
        if (imageUrls.size() >= 10) {
            throw new IllegalStateException("Cannot add more than 10 images per product");
        }
        
        imageUrls.add(imageUrl);
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        domainEvents.add(new ProductUpdatedEvent(this.id, "Added image: " + imageUrl));
    }
    
    /**
     * Remove product image
     */
    public void removeImage(String imageUrl) {
        ensureNotDiscontinued();
        
        if (imageUrl == null || imageUrl.isBlank()) {
            return; // Idempotent
        }
        
        boolean removed = imageUrls.remove(imageUrl);
        
        if (removed) {
            this.updatedAt = LocalDateTime.now();
            incrementVersion();
            domainEvents.add(new ProductUpdatedEvent(this.id, "Removed image: " + imageUrl));
        }
    }
    
    /**
     * Remove all images
     */
    public void clearImages() {
        ensureNotDiscontinued();
        
        if (!imageUrls.isEmpty()) {
            imageUrls.clear();
            this.updatedAt = LocalDateTime.now();
            incrementVersion();
            domainEvents.add(new ProductUpdatedEvent(this.id, "Cleared all images"));
        }
    }

    // Getters (no setters - encapsulation)
    
    public ProductId getId() {
        return id;
    }

    public SKU getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    public List<String> getImageUrls() {
        return new ArrayList<>(imageUrls); // Defensive copy
    }

    public Price getPrice() {
        return price;
    }

    public Stock getStock() {
        return stock;
    }

    public boolean isActive() {
        return active;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public boolean isAvailableForPurchase() {
        return availableForPurchase;
    }
    
    public boolean isDiscontinued() {
        return discontinued;
    }
    
    public int getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<ProductDomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
