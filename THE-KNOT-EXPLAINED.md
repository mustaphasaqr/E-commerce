# Order ↔ Product Communication: The "KNOT" 

## Current State: THE KNOT IS MISSING! 🔴

### What EXISTS Now:

```
Order Bounded Context:
✅ application/port/ProductPort.java (INTERFACE ONLY - not used anywhere!)
❌ No implementation of ProductPort
❌ No use case actually calls ProductPort
❌ No REST client to call Product API

Product Bounded Context:
❌ No REST API endpoints yet (no controllers!)
❌ No event subscribers in Order to listen to Product events
```

**Conclusion: Order and Product are ISOLATED - they don't talk to each other yet!**

---

## What SHOULD Exist (The Complete Knot):

### KNOT #1: Order → Product (Synchronous)

#### Step 1: Product exposes REST API

```
Product Side (Product Bounded Context):
──────────────────────────────────────

product/api/controller/ProductController.java:

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductFacade productFacade;
    
    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable String id) {
        return productFacade.getProductById(new GetProductByIdQuery(id));
    }
    
    @GetMapping
    public ProductResponse getProductBySku(@RequestParam String sku) {
        return productFacade.getProductBySku(new GetProductBySkuQuery(sku));
    }
    
    @PostMapping("/{id}/reserve-stock")
    public ProductResponse reserveStock(
        @PathVariable String id,
        @RequestBody ReserveStockRequest request
    ) {
        return productFacade.reserveStock(
            new ReserveStockCommand(id, request.orderId(), request.quantity())
        );
    }
}
```

#### Step 2: Order calls Product REST API

```
Order Side (Order Bounded Context):
──────────────────────────────────

order/infrastructure/adapter/product/ProductRestAdapter.java:

@Component
public class ProductRestAdapter implements ProductPort {
    
    private final RestTemplate restTemplate;
    private final String productServiceUrl; // e.g., http://localhost:8081
    
    @Override
    public boolean productExists(ProductId productId) {
        try {
            restTemplate.getForObject(
                productServiceUrl + "/api/products/" + productId.getValue(),
                ProductResponse.class
            );
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
    
    @Override
    public Money getProductPrice(ProductId productId) {
        ProductResponse response = restTemplate.getForObject(
            productServiceUrl + "/api/products/" + productId.getValue(),
            ProductResponse.class
        );
        return Money.of(response.price(), response.currency());
    }
    
    @Override
    public ProductInfo getProductInfo(ProductId productId) {
        ProductResponse response = restTemplate.getForObject(
            productServiceUrl + "/api/products/" + productId.getValue(),
            ProductResponse.class
        );
        
        return new ProductInfo(
            productId,
            response.name(),
            response.description(),
            Money.of(response.price(), response.currency()),
            response.active()
        );
    }
}
```

#### Step 3: Order use case calls ProductPort

```
order/application/usecase/PlaceOrderUseCase.java:

@Component
public class PlaceOrderUseCase {
    
    private final OrderRepository orderRepository;
    private final ProductPort productPort;  ← THE KNOT!
    private final InventoryPort inventoryPort;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Order execute(PlaceOrderCommand command) {
        // Validate products exist via ProductPort
        for (var item : command.getItems()) {
            ProductId productId = item.getProductId();
            
            // THE KNOT: Order calls Product!
            if (!productPort.productExists(productId)) {
                throw new ProductNotFoundException(productId);
            }
            
            // Validate price matches
            Money catalogPrice = productPort.getProductPrice(productId);
            if (!catalogPrice.equals(item.getUnitPrice())) {
                throw new PriceMismatchException(productId, item.getUnitPrice(), catalogPrice);
            }
            
            // Check stock availability via InventoryPort
            // (which internally calls Product's reserve-stock endpoint)
            if (!inventoryPort.checkAvailability(productId, item.getQuantity())) {
                throw new InsufficientStockException(productId);
            }
        }
        
        // Build and save order
        Order order = buildOrder(command);
        Order savedOrder = orderRepository.save(order);
        
        // Publish events
        savedOrder.getDomainEvents().forEach(eventPublisher::publish);
        savedOrder.clearDomainEvents();
        
        return savedOrder;
    }
}
```

---

### KNOT #2: Product → Order (Asynchronous)

#### Step 1: Product publishes events

```
Product Side:
────────────

product/application/usecase/UpdatePriceUseCase.java:

@Component
public class UpdatePriceUseCase {
    
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;
    
    public ProductResponse execute(UpdatePriceCommand command) {
        Product product = loadProduct(command.productId());
        
        // Domain logic (emits ProductPriceUpdatedEvent)
        product.updatePrice(newPrice);
        
        // Save
        Product saved = productRepository.save(product);
        
        // THE KNOT: Publish event to Kafka/RabbitMQ!
        saved.getDomainEvents().forEach(eventPublisher::publish);
        saved.clearDomainEvents();
        
        return ProductResponse.fromDomain(saved);
    }
}

product/infrastructure/messaging/DomainEventPublisherAdapter.java:

@Component
public class DomainEventPublisherAdapter implements DomainEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Override
    public void publish(ProductDomainEvent event) {
        // THE KNOT: Send to Kafka topic "product-events"
        kafkaTemplate.send("product-events", event.eventId().toString(), event);
        
        logger.info("Published {} to Kafka", event.getClass().getSimpleName());
    }
}
```

#### Step 2: Order subscribes to Product events

```
Order Side:
──────────

order/infrastructure/messaging/ProductEventListener.java:

@Component
public class ProductEventListener {
    
    private final OrderRepository orderRepository;
    private final Logger logger = LoggerFactory.getLogger(ProductEventListener.class);
    
    /**
     * THE KNOT: Listen to Product price changes
     */
    @KafkaListener(topics = "product-events", groupId = "order-service")
    public void handleProductEvent(ProductDomainEvent event) {
        
        if (event instanceof ProductPriceUpdatedEvent priceEvent) {
            handlePriceUpdate(priceEvent);
        } else if (event instanceof ProductDiscontinuedEvent discontinuedEvent) {
            handleProductDiscontinued(discontinuedEvent);
        }
    }
    
    private void handlePriceUpdate(ProductPriceUpdatedEvent event) {
        logger.info("Product price changed: {} - Old: {} → New: {}",
            event.productId(),
            event.oldPrice(),
            event.newPrice());
        
        // Business logic: Recalculate pending order totals?
        // Find all PENDING orders with this product
        List<Order> pendingOrders = orderRepository.findByStatusAndProductId(
            OrderStatus.PENDING,
            ProductId.of(event.productId())
        );
        
        // Notify customers or update order totals
        // (depends on your business rules)
    }
    
    private void handleProductDiscontinued(ProductDiscontinuedEvent event) {
        logger.warn("Product discontinued: {}", event.productId());
        
        // Business logic: Cancel pending orders? Notify customers?
    }
}
```

---

## THE COMPLETE FLOW

### Scenario: User places order for a laptop

```
1. USER → Order API
   POST /api/orders
   { "items": [{ "productId": "laptop-123", "quantity": 1, "price": 1000 }] }

2. PlaceOrderUseCase.execute()
   ↓
   productPort.productExists("laptop-123")  ← KNOT #1 (Order → Product)
   ↓
   HTTP GET http://product-service/api/products/laptop-123
   ↓
   Product API returns: { id, sku, name, price, stock, active }
   ↓
   ProductRestAdapter converts to ProductInfo
   ↓
   PlaceOrderUseCase validates price matches (1000 == 1000 ✓)
   ↓
   inventoryPort.reserveStock("laptop-123", 1)  ← Calls Product reserve API
   ↓
   HTTP POST http://product-service/api/products/laptop-123/reserve-stock
   ↓
   Product reserves stock (5 → 4 available)
   ↓
   Order saves with PENDING status

3. Later: Admin changes laptop price to 1200
   ↓
   UpdatePriceUseCase.execute()
   ↓
   product.updatePrice(1200)  (emits ProductPriceUpdatedEvent)
   ↓
   eventPublisher.publish(ProductPriceUpdatedEvent)  ← KNOT #2 (Product → Order)
   ↓
   Kafka topic "product-events" receives event
   ↓
   ProductEventListener.handleProductEvent()  ← Order consumes event
   ↓
   Order finds pending orders with laptop-123
   ↓
   Order decides: Keep old price for existing orders, use new price for new orders
```

---

## Summary: The TWO KNOTS

### KNOT #1: Order → Product (Synchronous - REST)
```
Order Use Case
    ↓ (calls)
ProductPort (interface in Order)
    ↓ (implemented by)
ProductRestAdapter (in Order infrastructure)
    ↓ (HTTP calls)
Product REST API
    ↓ (delegates to)
ProductFacade
    ↓ (executes)
Product Use Case
```

**Files to create:**
- ✅ ProductPort.java (EXISTS in Order)
- ❌ ProductRestAdapter.java (MISSING - implement this)
- ❌ ProductController.java (MISSING - create REST API)

### KNOT #2: Product → Order (Asynchronous - Events)
```
Product Use Case
    ↓ (saves aggregate)
Product Aggregate (emits events)
    ↓ (publishes via)
DomainEventPublisher
    ↓ (implemented by)
DomainEventPublisherAdapter (Kafka)
    ↓ (sends to topic)
Kafka "product-events"
    ↓ (consumed by)
ProductEventListener (in Order)
    ↓ (reacts)
Order business logic
```

**Files to create:**
- ✅ DomainEventPublisher interface (EXISTS in both)
- ✅ DomainEventPublisherAdapter (EXISTS in both)
- ❌ ProductEventListener.java (MISSING in Order - create this)
- ❌ Update use cases to publish events (MISSING - fix this)

---

## What's MISSING in Your Current Code:

### Product Side:
1. ❌ REST API Controllers (no Product API yet!)
2. ❌ Event publishing in use cases (use cases don't publish events!)
3. ❌ ProductResponse DTO (needed by API)

### Order Side:
1. ❌ ProductRestAdapter.java (ProductPort has no implementation!)
2. ❌ ProductEventListener.java (no event subscribers!)
3. ❌ PlaceOrderUseCase doesn't use ProductPort (no validation!)

### Shared:
1. ❌ Kafka/RabbitMQ configuration
2. ❌ RestTemplate/WebClient configuration

---

## Next Steps to CREATE the KNOTS:

1. **Update Product use cases** → Add event publishing
2. **Create ProductResponse DTO** → Needed by both layers
3. **Create Product REST API** → ProductController
4. **Create ProductRestAdapter** → Implement ProductPort in Order
5. **Create ProductEventListener** → Subscribe to events in Order
6. **Configure messaging** → Kafka/RabbitMQ setup

Want me to start implementing these KNOTS?
