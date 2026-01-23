# E-Commerce Architecture: Product vs Order Bounded Contexts

## THE GAME: Hexagonal Architecture + DDD

### 1. What are PORTS?

**Ports = Interfaces that define what the application NEEDS from outside**

```
┌─────────────────────────────────────────┐
│      PRODUCT BOUNDED CONTEXT            │
│                                         │
│  ┌───────────────────────────────┐     │
│  │   DOMAIN LAYER                │     │
│  │   - Product (aggregate)       │     │
│  │   - Value Objects             │     │
│  │   - Domain Events             │     │
│  └───────────────────────────────┘     │
│              ↕                          │
│  ┌───────────────────────────────┐     │
│  │   APPLICATION LAYER           │     │
│  │   - Use Cases                 │     │
│  │   - ProductFacade             │     │
│  │   - Commands                  │     │
│  └───────────────────────────────┘     │
│              ↕                          │
│  ┌───────────────────────────────┐     │
│  │   APPLICATION PORTS           │  ← THESE ARE INTERFACES
│  │   (What Product NEEDS)        │     │
│  │                               │     │
│  │   - ProductRepository         │  ← Save/Load products
│  │   - EventPublisher            │  ← Publish domain events
│  └───────────────────────────────┘     │
│              ↕                          │
│  ┌───────────────────────────────┐     │
│  │   INFRASTRUCTURE LAYER        │  ← IMPLEMENTS THE PORTS
│  │   (Adapters)                  │     │
│  │                               │     │
│  │   - ProductJpaRepository      │  ← Uses PostgreSQL
│  │   - KafkaEventPublisher       │  ← Uses Kafka
│  └───────────────────────────────┘     │
│              ↕                          │
│  ┌───────────────────────────────┐     │
│  │   API LAYER                   │     │
│  │   - ProductController         │  ← REST endpoints
│  └───────────────────────────────┘     │
└─────────────────────────────────────────┘
```

---

## 2. Does Product Need Ports? **YES - but MINIMAL**

### Product Ports (application/port/):

```java
// What Product NEEDS to function:

1. ProductRepository (MANDATORY)
   - Save/load products from database
   - Check SKU uniqueness
   
2. EventPublisher (MANDATORY)
   - Publish ProductCreatedEvent
   - Publish ProductPriceUpdatedEvent
   - Publish StockUpdatedEvent
   - etc.

3. NO OTHER PORTS NEEDED
   - Product is SELF-SUFFICIENT
   - Product doesn't care about orders
   - Product doesn't care about payments
   - Product doesn't care about shipments
```

---

## 3. Order Needs MORE Ports (Order talks to external systems)

### Order Ports (order/application/port/):

```java
// What Order NEEDS to function:

1. OrderRepository (MANDATORY)
   - Save/load orders
   
2. EventPublisher (MANDATORY)
   - Publish OrderPlacedEvent
   - Publish OrderCancelledEvent
   
3. ProductQueryPort (MANDATORY) ← KEY DIFFERENCE
   - Check if product exists
   - Check if product has stock
   - Get product price
   
4. PaymentGatewayPort (MANDATORY)
   - Process payment
   - Refund payment
   
5. NotificationPort (OPTIONAL)
   - Send order confirmation email
   - Send shipment notification
```

---

## 4. THE KEY QUESTION: How do Order and Product talk?

### Scenario: User places an order

```
USER → Order API → OrderFacade → PlaceOrderUseCase
                                      ↓
                            Need to check product stock!
                                      ↓
                            ProductQueryPort (interface in Order)
                                      ↓
                            ProductRestClient (adapter in Order infrastructure)
                                      ↓
                            HTTP GET /products/{id} ← Calls Product API
                                      ↓
                            Product API → ProductFacade → GetProductByIdUseCase
                                      ↓
                            Returns ProductResponse
                                      ↓
                            Order validates stock is available
                                      ↓
                            If OK → Reserve stock via Product API
                                      ↓
                            HTTP POST /products/{id}/reserve-stock
                                      ↓
                            Product API → ProductFacade → ReserveStockUseCase
                                      ↓
                            Product aggregate reserves stock
                                      ↓
                            Publishes StockUpdatedEvent (async)
                                      ↓
                            Order saves with RESERVED status
```

### Communication Patterns:

#### SYNCHRONOUS (Order → Product):
```
Order needs immediate answer:
- "Does this product exist?"
- "Is stock available?"
- "Reserve stock now!"

Solution: Order has ProductQueryPort
         Infrastructure implements it with REST client
         Calls Product's REST API
```

#### ASYNCHRONOUS (Product → Order):
```
Product doesn't care about orders:
- Product price changed
- Product discontinued
- Stock updated

Solution: Product publishes events
         Order subscribes to events
         Order reacts (e.g., recalculate order totals)
```

---

## 5. External Systems: Shared vs Specific

### SHARED INFRASTRUCTURE (both use):
```
┌─────────────────────────────────────┐
│   SHARED INFRASTRUCTURE             │
│                                     │
│   - PostgreSQL Database             │  ← Separate tables
│     * product_table                 │     (product_*)
│     * order_table                   │     (order_*)
│                                     │
│   - Kafka Event Bus                 │  ← Both publish/subscribe
│     * product.events topic          │
│     * order.events topic            │
│                                     │
│   - Redis Cache                     │  ← Separate key spaces
│     * product:* keys                │
│     * order:* keys                  │
└─────────────────────────────────────┘
```

### SPECIFIC TO ORDER (Order only):
```
┌─────────────────────────────────────┐
│   ORDER-SPECIFIC SYSTEMS            │
│                                     │
│   - Payment Gateway (Stripe)        │  ← Only Order needs
│   - Shipping API                    │  ← Only Order needs
│   - Notification Service            │  ← Only Order needs
└─────────────────────────────────────┘
```

### SPECIFIC TO PRODUCT (Product only):
```
┌─────────────────────────────────────┐
│   PRODUCT-SPECIFIC SYSTEMS          │
│                                     │
│   - Image Storage (S3)              │  ← Only Product needs
│   - Search Engine (Elasticsearch)   │  ← Only Product needs
│   - Inventory Management System     │  ← Only Product needs
└─────────────────────────────────────┘
```

---

## 6. Why NO OrderQueryPort in Product?

### ❌ WRONG ARCHITECTURE:
```java
// ProductUpdatePriceUseCase - BAD!

public ProductResponse execute(UpdatePriceCommand command) {
    Product product = loadProduct(command.productId());
    
    // WRONG: Product checking Order!
    List<Order> activeOrders = orderQueryPort.findActiveOrdersForProduct(productId);
    if (!activeOrders.isEmpty()) {
        throw new CannotChangePriceException("Product has active orders");
    }
    
    product.updatePrice(newPrice);
    return save(product);
}
```

**Why wrong?**
- Product bounded context DEPENDS on Order bounded context
- Violates bounded context independence
- Hard to test Product without Order
- Creates circular dependency

### ✅ CORRECT ARCHITECTURE:
```java
// ProductUpdatePriceUseCase - GOOD!

public ProductResponse execute(UpdatePriceCommand command) {
    Product product = loadProduct(command.productId());
    
    // Domain validates independently
    product.updatePrice(newPrice); // Validates 10x/90% limits internally
    
    Product saved = save(product);
    
    // Infrastructure publishes event
    // Order will react if needed (async)
    return ProductResponse.fromDomain(saved);
}
```

**Why correct?**
- Product is SELF-SUFFICIENT
- Product validates its own business rules
- Order SUBSCRIBES to ProductPriceUpdatedEvent
- Order decides how to react (recalculate totals, notify customer, etc.)
- No coupling between bounded contexts

---

## 7. THE COMPLETE FLOW

### Example: User buys a laptop

```
1. ORDER CREATION:
   User → POST /orders
   ↓
   Order → ProductQueryPort → GET /products/laptop-123
   ↓
   Product returns: { id, sku, price: 1000, stock: 5 }
   ↓
   Order validates: Price matches, stock available
   ↓
   Order → ProductQueryPort → POST /products/laptop-123/reserve-stock
   ↓
   Product reserves stock (5 → 4 available)
   ↓
   Product publishes StockUpdatedEvent
   ↓
   Order saves with PENDING_PAYMENT status

2. PAYMENT:
   Order → PaymentGatewayPort → Stripe API
   ↓
   Payment succeeds
   ↓
   Order updates to PAID status
   ↓
   Order publishes OrderPaidEvent

3. FULFILLMENT:
   Warehouse system subscribes to OrderPaidEvent
   ↓
   Warehouse ships item
   ↓
   Warehouse → POST /products/laptop-123/fulfill-reservation
   ↓
   Product fulfills reservation (reserved: 1 → 0, total: 4 → 4)
   ↓
   Product publishes StockUpdatedEvent

4. PRICE CHANGE (meanwhile):
   Admin → PUT /products/laptop-123/price { newPrice: 1200 }
   ↓
   Product validates: 1200 ≤ 10,000 (10x limit) ✓
   ↓
   Product updates price
   ↓
   Product publishes ProductPriceUpdatedEvent
   ↓
   Order subscribes, sees existing orders still use old price (1000)
   ↓
   NEW orders will use new price (1200)
```

---

## 8. SUMMARY: The Rules

### Product Bounded Context:
- ✅ **Needs ports**: ProductRepository, EventPublisher
- ❌ **Does NOT need**: OrderQueryPort, PaymentGatewayPort
- 🎯 **Philosophy**: Self-sufficient, validates own rules

### Order Bounded Context:
- ✅ **Needs ports**: OrderRepository, ProductQueryPort, PaymentGatewayPort, EventPublisher
- ❌ **Does NOT need**: Direct access to Product domain models
- 🎯 **Philosophy**: Orchestrates, integrates, depends on other contexts

### Communication:
- **Synchronous** (Order → Product): REST API via ProductQueryPort
- **Asynchronous** (Product → Order): Events via EventPublisher
- **Shared Infrastructure**: Database, Kafka, Redis (isolated namespaces)
- **Specific Systems**: Payment (Order only), Search (Product only)

### Why Reviewer Said "NO OrderQueryPort in Product":
- Product doesn't need to know about orders
- Product validates price changes independently (10x/90% rules)
- Order reacts to price changes via events (async)
- **Bounded contexts should be INDEPENDENT**

---

## 9. Your Product Ports (Minimal):

Create these in `product/application/port/`:

```
product/application/port/
├── ProductRepository.java    ← Already exists in domain!
└── EventPublisher.java        ← NEW (if not shared)
```

**Wait!** ProductRepository is already in `domain/repository/` ✓

So you ONLY need:
- **EventPublisher** (if you want a Product-specific interface)
- OR use a shared EventPublisher from common infrastructure

**Decision**: 
- If EventPublisher is SHARED across Order and Product → Put in shared/infrastructure
- If Product needs Product-SPECIFIC event publishing → Create port in product/application/port/

Most projects use **SHARED EventPublisher** → NO product-specific ports needed!

---

## Conclusion:

**Product ports** = ProductRepository (domain) + EventPublisher (shared)
**Order ports** = OrderRepository + ProductQueryPort + PaymentGatewayPort + EventPublisher

**Product is included in Order** = Order CALLS Product's API (synchronous)
**Both talk to external systems** = Via their own ports + shared infrastructure
**External systems** = Some shared (DB, Kafka), some specific (Payment for Order)

🎯 **Bottom line**: Product needs MINIMAL ports. Order needs MANY ports.
