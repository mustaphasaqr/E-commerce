# Shipping Integration - Production TODOs

## Overview
The shipping integration with Aramex is **functionally complete** but uses **placeholder data** for development. This document outlines all remaining work needed for production deployment.

---

## 🔴 Critical - Required for Production

### 1. Add Delivery Address to Order Model
**Status**: 🔲 Not Started  
**Priority**: CRITICAL  
**Effort**: Medium (2-3 hours)  

**Current State**:
- Order model has NO delivery address fields
- ShipOrderUseCase uses hardcoded placeholder addresses:
  ```java
  "123 Main Street", // Placeholder
  "Cairo",           // Placeholder
  "11511",           // Placeholder
  "EG"               // Placeholder
  ```

**Required Changes**:
1. **Add fields to Order domain model** ([Order.java](src/main/java/com/mustapha/ecommerce/order/domain/model/Order.java)):
   ```java
   // Delivery address - populated during order placement
   private String deliveryFullName;      // Recipient name
   private String deliveryCompany;       // Optional company name
   private String deliveryAddressLine1;  // Street address, P.O. box
   private String deliveryAddressLine2;  // Apt, suite, unit, building, floor, etc
   private String deliveryCity;          // City/District
   private String deliveryState;         // State/Province/Region
   private String deliveryPostalCode;    // ZIP/Postal code
   private String deliveryCountry;       // ISO 3166-1 alpha-2 (EG, AE, SA, etc)
   private String deliveryPhone;         // Contact phone number
   private String deliveryEmail;         // Optional contact email
   ```

2. **Update OrderJpaEntity** ([OrderJpaEntity.java](src/main/java/com/mustapha/ecommerce/order/infrastructure/persistence/entity/OrderJpaEntity.java)):
   ```java
   @Column(name = "delivery_full_name", length = 100)
   private String deliveryFullName;
   
   @Column(name = "delivery_company", length = 100)
   private String deliveryCompany;
   
   @Column(name = "delivery_address_line1", length = 200, nullable = false)
   private String deliveryAddressLine1;
   
   @Column(name = "delivery_address_line2", length = 200)
   private String deliveryAddressLine2;
   
   @Column(name = "delivery_city", length = 100, nullable = false)
   private String deliveryCity;
   
   @Column(name = "delivery_state", length = 100)
   private String deliveryState;
   
   @Column(name = "delivery_postal_code", length = 20, nullable = false)
   private String deliveryPostalCode;
   
   @Column(name = "delivery_country", length = 2, nullable = false)
   private String deliveryCountry;
   
   @Column(name = "delivery_phone", length = 20, nullable = false)
   private String deliveryPhone;
   
   @Column(name = "delivery_email", length = 100)
   private String deliveryEmail;
   ```

3. **Create database migration** (Flyway or Liquibase):
   ```sql
   ALTER TABLE orders 
   ADD COLUMN delivery_full_name VARCHAR(100),
   ADD COLUMN delivery_company VARCHAR(100),
   ADD COLUMN delivery_address_line1 VARCHAR(200) NOT NULL,
   ADD COLUMN delivery_address_line2 VARCHAR(200),
   ADD COLUMN delivery_city VARCHAR(100) NOT NULL,
   ADD COLUMN delivery_state VARCHAR(100),
   ADD COLUMN delivery_postal_code VARCHAR(20) NOT NULL,
   ADD COLUMN delivery_country VARCHAR(2) NOT NULL,
   ADD COLUMN delivery_phone VARCHAR(20) NOT NULL,
   ADD COLUMN delivery_email VARCHAR(100);
   ```

4. **Update PlaceOrderCommand** to accept delivery address ([PlaceOrderCommand.java](src/main/java/com/mustapha/ecommerce/order/application/command/PlaceOrderCommand.java)):
   ```java
   public class PlaceOrderCommand {
       private final CustomerId customerId;
       private final List<OrderItemData> items;
       private final DeliveryAddress deliveryAddress; // NEW
   }
   
   public record DeliveryAddress(
       String fullName,
       String company,
       String addressLine1,
       String addressLine2,
       String city,
       String state,
       String postalCode,
       String country,
       String phone,
       String email
   ) {}
   ```

5. **Update OrderRequest DTO** ([OrderRequest.java](src/main/java/com/mustapha/ecommerce/order/dto/OrderRequest.java)):
   ```java
   public class OrderRequest {
       @NotBlank
       private String customerId;
       
       @NotEmpty
       @Valid
       private List<OrderItemRequest> items;
       
       @NotNull // NEW
       @Valid
       private DeliveryAddressRequest deliveryAddress;
   }
   
   public class DeliveryAddressRequest {
       @NotBlank(message = "Full name is required")
       private String fullName;
       
       private String company;
       
       @NotBlank(message = "Address is required")
       @Size(max = 200)
       private String addressLine1;
       
       @Size(max = 200)
       private String addressLine2;
       
       @NotBlank(message = "City is required")
       private String city;
       
       private String state;
       
       @NotBlank(message = "Postal code is required")
       private String postalCode;
       
       @NotBlank(message = "Country code is required")
       @Pattern(regexp = "[A-Z]{2}", message = "Country must be 2-letter ISO code")
       private String country;
       
       @NotBlank(message = "Phone is required")
       @Pattern(regexp = "\\+?[0-9]{10,15}", message = "Invalid phone number")
       private String phone;
       
       @Email
       private String email;
   }
   ```

6. **Update ShipOrderUseCase** ([ShipOrderUseCase.java](src/main/java/com/mustapha/ecommerce/order/application/usecase/ShipOrderUseCase.java)):
   Replace placeholder lines with:
   ```java
   var shipmentRequest = new ShippingPort.ShipmentRequest(
       order.getId(),
       order.getDeliveryFullName(),
       order.getDeliveryCompany() != null ? order.getDeliveryCompany() : "",
       order.getDeliveryAddressLine1(),
       order.getDeliveryAddressLine2() != null ? order.getDeliveryAddressLine2() : "",
       order.getDeliveryCity(),
       order.getDeliveryState() != null ? order.getDeliveryState() : "",
       order.getDeliveryPostalCode(),
       order.getDeliveryCountry(),
       order.getDeliveryPhone(),
       order.getDeliveryEmail() != null ? order.getDeliveryEmail() : "",
       calculatePackageWeight(order.getItems()), // From TODO #2
       packageConfig.getLength(), // From TODO #4
       packageConfig.getWidth(),
       packageConfig.getHeight(),
       "E-commerce order #" + order.getId().getValue(),
       order.getTotalAmount().getAmount(),
       getServiceType(order) // Could be based on customer choice, order total, etc.
   );
   ```

**Testing**:
- Unit tests with valid/invalid addresses
- Integration tests with Aramex MOCK mode
- End-to-end test: Place order → Ship → Verify address sent to Aramex

**References**:
- Lines marked with `// TODO: Get from order delivery address` in ShipOrderUseCase.java
- Lines 77-89 in ShipOrderUseCase.java

---

### 2. Calculate Package Weight from OrderItems
**Status**: 🔲 Not Started  
**Priority**: HIGH  
**Effort**: Small (1-2 hours)  

**Current State**:
- Fixed weight: `2.0 kg` hardcoded in ShipOrderUseCase
- ShipmentRequest always sends 2.0 regardless of order contents

**Required Changes**:

1. **Add weight field to Product** (if you have a Product entity):
   ```java
   @Column(name = "weight_kg")
   private Double weightKg; // Weight in kilograms
   ```

2. **OR: Add weight to OrderItem**:
   ```java
   public class OrderItem {
       private ProductId productId;
       private String productName;
       private int quantity;
       private Money unitPrice;
       private Double weightKg; // NEW: Weight per unit in kg
   }
   ```

3. **Add weight calculation method to Order** ([Order.java](src/main/java/com/mustapha/ecommerce/order/domain/model/Order.java)):
   ```java
   /**
    * Calculate total package weight from all items
    * Returns total weight in kilograms
    * Assumes each product has weight stored in weightKg
    * 
    * @return Total weight in kg, or default 2.0 if no weight data
    */
   public double calculateTotalWeight() {
       if (items == null || items.isEmpty()) {
           return 2.0; // Default for empty orders
       }
       
       double totalWeight = items.stream()
           .filter(item -> item.getWeightKg() != null)
           .mapToDouble(item -> item.getWeightKg() * item.getQuantity())
           .sum();
       
       return totalWeight > 0 ? totalWeight : 2.0; // Fallback to default
   }
   ```

4. **Update ShipOrderUseCase** line 95:
   ```java
   // Before:
   2.0, // TODO: Calculate from items
   
   // After:
   order.calculateTotalWeight(), // Calculated from items
   ```

**Database Migration**:
```sql
-- Option 1: Add to products table
ALTER TABLE products ADD COLUMN weight_kg DECIMAL(10, 3);

-- Option 2: Add to order_items table
ALTER TABLE order_items ADD COLUMN weight_kg DECIMAL(10, 3);
```

**Testing**:
- Test with orders containing different quantities
- Test with missing weight data (should use fallback)
- Test with heavy items (>30 kg might need different shipping)

**References**:
- Line 95 in ShipOrderUseCase.java: `2.0, // TODO: Calculate from items`

---

### 3. Get Customer Contact Information
**Status**: 🔲 Not Started  
**Priority**: MEDIUM  
**Effort**: Medium (2-3 hours)  

**Current State**:
- Customer name: "Customer Name" (placeholder)
- Customer phone: "+201234567890" (placeholder)
- Customer email: "" (empty)
- These are hardcoded in ShipOrderUseCase lines 78-91

**Option A: Customer Aggregate** (Recommended - proper DDD):

1. **Create Customer entity**:
   ```java
   @Entity
   @Table(name = "customers")
   public class CustomerJpaEntity {
       @Id
       private String id; // Maps to CustomerId.getValue()
       
       @Column(nullable = false)
       private String fullName;
       
       @Column(nullable = false)
       private String email;
       
       @Column(nullable = false)
       private String phone;
       
       // Optional: Default delivery address
       private String defaultAddressLine1;
       private String defaultAddressLine2;
       private String defaultCity;
       private String defaultState;
       private String defaultPostalCode;
       private String defaultCountry;
   }
   ```

2. **Create CustomerRepository**:
   ```java
   public interface CustomerRepository {
       Optional<Customer> findById(CustomerId customerId);
       Customer save(Customer customer);
   }
   ```

3. **Update ShipOrderUseCase** to load customer:
   ```java
   // In constructor, inject CustomerRepository
   private final CustomerRepository customerRepository;
   
   // In execute(), load customer
   Customer customer = customerRepository.findById(order.getCustomerId())
       .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
   
   // Use real customer data:
   var shipmentRequest = new ShippingPort.ShipmentRequest(
       order.getId(),
       customer.getFullName(), // Real name
       // ... other fields ...
       customer.getPhone(),   // Real phone
       customer.getEmail()    // Real email
       // ...
   );
   ```

**Option B: Store in Order** (Simpler - denormalized):
- Add `customerName`, `customerPhone`, `customerEmail` to Order model
- Copy from Customer aggregate during order placement
- Pros: No extra repository call, snapshot at order time
- Cons: Data duplication, might become stale

**Decision**: Choose Option A for proper DDD, or Option B for simplicity.

**References**:
- Lines 78, 86, 90 in ShipOrderUseCase.java

---

## 🟡 Medium Priority - Production Enhancements

### 4. Make Package Dimensions Configurable
**Status**: 🔲 Not Started  
**Priority**: MEDIUM  
**Effort**: Small (30 minutes)  

**Current State**:
- Hardcoded dimensions: 30cm × 20cm × 15cm (line 96-98 in ShipOrderUseCase)

**Required Changes**:

1. **Add to application.properties**:
   ```properties
   # Default package dimensions (in centimeters)
   # These are used when actual product dimensions are not available
   shipping.default-package-length=30.0
   shipping.default-package-width=20.0
   shipping.default-package-height=15.0
   
   # Package selection strategy
   # OPTIONS: FIXED (use default), CALCULATED (from items), AUTO (smart selection)
   shipping.package-sizing-strategy=FIXED
   ```

2. **Create PackageConfig**:
   ```java
   @Configuration
   @ConfigurationProperties(prefix = "shipping")
   public class PackageConfig {
       private Double defaultPackageLength = 30.0;
       private Double defaultPackageWidth = 20.0;
       private Double defaultPackageHeight = 15.0;
       private String packageSizingStrategy = "FIXED";
       
       // Getters/setters
   }
   ```

3. **Inject into ShipOrderUseCase**:
   ```java
   private final PackageConfig packageConfig;
   
   // In execute():
   packageConfig.getDefaultPackageLength(),
   packageConfig.getDefaultPackageWidth(),
   packageConfig.getDefaultPackageHeight(),
   ```

**Advanced Option** (if products have dimensions):
```java
/**
 * Calculate minimum box size needed for all items
 * Uses bin packing algorithm or simple max dimensions
 */
private PackageDimensions calculatePackageDimensions(List<OrderItem> items) {
    // Simple approach: Find largest item dimensions
    double maxLength = items.stream()
        .mapToDouble(item -> item.getProduct().getLength())
        .max()
        .orElse(packageConfig.getDefaultPackageLength());
    
    // Add padding for packaging material
    return new PackageDimensions(
        maxLength + 5.0,  // +5cm padding
        maxWidth + 5.0,
        maxHeight + 5.0
    );
}
```

**References**:
- Lines 96-98 in ShipOrderUseCase.java

---

### 5. Aramex API Credentials
**Status**: ⏳ BLOCKED - Waiting for Aramex  
**Priority**: HIGH  
**Effort**: None (business process, not coding)  

**Current State**:
- Using MOCK mode (generates fake tracking numbers)
- No real Aramex API calls

**Required Steps**:
1. Contact Aramex sales/business team:
   - Egypt: https://www.aramex.com/eg/en/contact-us
   - UAE: https://www.aramex.com/ae/en/contact-us
   - Saudi: https://www.aramex.com/sa/en/contact-us

2. Provide business information:
   - Company registration documents
   - Business license
   - Expected monthly shipment volume
   - Billing information

3. Wait for credentials (1-7 business days):
   - Username
   - Password
   - Account Number
   - Account PIN
   - Account Entity (DXB, JED, RUH, etc.)

4. Add credentials to `.env`:
   ```bash
   ARAMEX_USERNAME=your_username
   ARAMEX_PASSWORD=your_password
   ARAMEX_ACCOUNT_NUMBER=your_account_number
   ARAMEX_ACCOUNT_PIN=your_pin
   ARAMEX_ACCOUNT_ENTITY=DXB
   ```

5. Test with real API:
   ```powershell
   .\start-with-real-apis.ps1
   ```

**Testing after credentials**:
- Create test shipment
- Verify tracking number format (not MOCK-*)
- Download shipping label PDF
- Test tracking updates

**References**:
- [ARAMEX_SETUP.md](ARAMEX_SETUP.md) - Complete credential guide
- [HOW_TO_GET_ARAMEX_CREDENTIALS.md](HOW_TO_GET_ARAMEX_CREDENTIALS.md)

---

### 6. Service Type Selection
**Status**: 🔲 Not Started  
**Priority**: LOW  
**Effort**: Small (1 hour)  

**Current State**:
- Fixed service type: `"EXPRESS"` (line 100 in ShipOrderUseCase)

**Enhancement Options**:

1. **Customer choice during checkout**:
   ```java
   // Add to PlaceOrderCommand
   private final ShippingServiceType serviceType;
   
   // Store in Order
   private ShippingServiceType shippingServiceType;
   ```

2. **Based on business logic**:
   ```java
   private String determineServiceType(Order order) {
       // Express for orders > 1000 EGP
       if (order.getTotalAmount().getAmount() > 1000.0) {
           return "EXPRESS"; // 1-2 days, higher cost
       }
       
       // Standard for regular orders
       if (order.getTotalAmount().getAmount() > 200.0) {
           return "STANDARD"; // 3-5 days, medium cost
       }
       
       // Economy for small orders
       return "ECONOMY"; // 5-7 days, lowest cost
   }
   ```

3. **Premium customer tier**:
   ```java
   if (customer.isPremiumMember()) {
       return "EXPRESS"; // Free express for premium
   }
   ```

**Available Aramex Services**:
- `PPX` - Priority Parcel Express (1-2 days)
- `PDX` - Parcel Domestic Express (2-3 days)
- `PLX` - Parcel Long Haul Express (3-5 days)
- `DDX` - Deferred Delivery (5-7 days, cheapest)
- `GRD` - Ground (7-10 days)

**References**:
- Line 100 in ShipOrderUseCase.java: `"EXPRESS" // Default service type`

---

## 🟢 Low Priority - Nice to Have

### 7. Shipping Label Attachment
**Status**: 🔲 Not Started  
**Priority**: LOW  
**Effort**: Medium (2 hours)  

**Current State**:
- Label URL returned in ShipmentResult
- Logged to console (line 107)
- Not stored or accessible later

**Enhancement**:
1. Store label URL in Order:
   ```java
   private String shippingLabelUrl; // Aramex PDF label
   ```

2. Add to OrderResponse DTO:
   ```java
   public class OrderResponse {
       // ... existing fields ...
       private String shippingLabelUrl;
   }
   ```

3. Create endpoint to download/view label:
   ```java
   @GetMapping("/{orderId}/shipping-label")
   public ResponseEntity<byte[]> getShippingLabel(@PathVariable Long orderId) {
       // Fetch label PDF from Aramex URL
       // Return as PDF download
   }
   ```

4. Send label to customer in shipping notification email

---

### 8. Comprehensive Aramex Integration Tests
**Status**: 🔲 Not Started  
**Priority**: LOW  
**Effort**: Large (4-6 hours)  

**Current State**:
- 65 shipping tests (use case, events, config)
- No dedicated tests for Aramex client/adapter

**Recommended Test Suites**:

1. **AramexConfigTest** (~26 tests):
   - Credential validation
   - Environment variable loading
   - isConfigured() logic
   - Fallback values

2. **AramexShippingClientTest** (~24 tests):
   - createShipment() success/failure
   - trackShipment() with valid/invalid tracking
   - getShippingRates() for different destinations
   - cancelShipment() scenarios
   - MOCK mode behavior
   - HTTP error handling (404, 500, timeout)
   - Resilience patterns (circuit breaker, retry)

3. **ShippingAdapterTest** (~10 tests):
   - DTO conversion (ShipmentRequest → Aramex format)
   - Fallback to MANUAL mode
   - Error message mapping
   - Circuit breaker triggers

**Reference**: Similar coverage to payment (86 tests) and email (45 tests)

---

### 9. Real-time Tracking Updates
**Status**: 🔲 Not Started  
**Priority**: LOW  
**Effort**: Large (6-8 hours)  

**Current State**:
- Tracking number stored in Order
- No tracking updates after shipment

**Enhancement**:
1. **Webhook endpoint** for Aramex tracking events:
   ```java
   @PostMapping("/webhooks/aramex/tracking")
   public ResponseEntity<Void> handleTrackingUpdate(
       @RequestBody TrackingUpdateRequest request) {
       // Update order status based on tracking event
       // Notify customer of delivery progress
   }
   ```

2. **Scheduled polling** (if webhooks not available):
   ```java
   @Scheduled(fixedRate = 3600000) // Every hour
   public void updateShipmentStatuses() {
       List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);
       
       for (Order order : shippedOrders) {
           TrackingResponse tracking = shippingProvider.trackShipment(order.getTrackingNumber());
           
           if (tracking.isDelivered()) {
               order.deliver(LocalDateTime.now());
               orderRepository.save(order);
           }
       }
   }
   ```

3. **Tracking page** for customers:
   ```java
   @GetMapping("/orders/{orderId}/tracking")
   public TrackingResponse getTrackingInfo(@PathVariable Long orderId) {
       Order order = orderRepository.findById(orderId);
       return shippingProvider.trackShipment(order.getTrackingNumber());
   }
   ```

---

### 10. Multi-Package Orders
**Status**: 🔲 Not Started  
**Priority**: LOW  
**Effort**: Large (8-10 hours)  

**Current State**:
- One shipment per order
- All items in single package

**Enhancement** (for large orders):
1. Split order into multiple packages if:
   - Total weight > 30 kg
   - Total volume > 0.5 m³
   - Items require different handling (fragile + non-fragile)

2. Create multiple shipments:
   ```java
   List<PackageGroup> packages = splitOrderIntoPackages(order);
   
   List<String> trackingNumbers = new ArrayList<>();
   for (PackageGroup pkg : packages) {
       var shipmentResult = shippingPort.createShipment(pkg.toRequest());
       trackingNumbers.add(shipmentResult.trackingNumber());
   }
   
   order.ship(String.join(", ", trackingNumbers), carrier);
   ```

3. Store multiple tracking numbers

---

## Summary - Implementation Priority

### Phase 1 - Essential for Production (Week 1-2)
1. ✅ Add delivery address to Order model
2. ✅ Calculate package weight from items
3. ✅ Get customer contact information (choose Option A or B)

### Phase 2 - Production Enhancements (Week 3)
4. ✅ Make package dimensions configurable
5. ⏳ Obtain Aramex API credentials (business process, parallel to Phase 1)
6. ✅ Service type selection logic

### Phase 3 - Quality & UX (Week 4+)
7. ✅ Store shipping label URLs
8. ✅ Comprehensive Aramex integration tests (60+ tests)
9. ✅ Real-time tracking updates
10. ✅ Multi-package order support

---

## File References

| File | Lines | Description |
|------|-------|-------------|
| [ShipOrderUseCase.java](src/main/java/com/mustapha/ecommerce/order/application/usecase/ShipOrderUseCase.java) | 77-100 | Main file with all TODOs |
| [Order.java](src/main/java/com/mustapha/ecommerce/order/domain/model/Order.java) | 20-40 | Add delivery address fields here |
| [application.properties](src/main/resources/application.properties) | 213-229 | Shipping configuration |
| [ShippingPort.java](src/main/java/com/mustapha/ecommerce/order/application/port/ShippingPort.java) | 1-60 | Port interface |
| [ShippingAdapter.java](src/main/java/com/mustapha/ecommerce/order/infrastructure/adapter/shipping/ShippingAdapter.java) | 1-150 | Adapter implementation |
| [AramexShippingClient.java](src/main/java/com/mustapha/ecommerce/order/infrastructure/adapter/shipping/aramex/AramexShippingClient.java) | Full file | Aramex REST client |

---

## Testing Strategy

1. **Unit Tests** (Already complete - 65 tests):
   - ShipOrderUseCaseTest: 26 tests ✅
   - OrderShippedEventTest: 14 tests ✅
   - ShippingConfigTest: 25 tests ✅

2. **Integration Tests** (TODO):
   - AramexConfigTest: ~26 tests
   - AramexShippingClientTest: ~24 tests
   - ShippingAdapterTest: ~10 tests

3. **E2E Tests** (After production TODOs):
   - Place order with real address → Ship → Verify Aramex API call
   - Track shipment → Get updates → Verify delivery

---

## Questions?

- **Aramex Setup**: See [ARAMEX_SETUP.md](ARAMEX_SETUP.md)
- **Credential Guide**: See [HOW_TO_GET_ARAMEX_CREDENTIALS.md](HOW_TO_GET_ARAMEX_CREDENTIALS.md)
- **Architecture**: See [ARCHITECTURE-EXPLANATION.md](ARCHITECTURE-EXPLANATION.md)
- **API Testing**: Use `test-auth.ps1` for API endpoint testing

---

**Last Updated**: March 1, 2026  
**Status**: Aramex integration complete, awaiting production data implementation  
**Next Step**: Start with Phase 1 - Add delivery address to Order model
