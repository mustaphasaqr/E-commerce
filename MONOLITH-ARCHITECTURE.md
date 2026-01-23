# Monolith Architecture

## Decision: Modular Monolith vs Microservices

**Chosen Architecture**: **Modular Monolith** with clear migration path to Microservices

### Why Monolith?

**Timeline Comparison:**
- **Monolith**: 2-3 weeks ✅
- **Microservices**: 6-8 weeks ❌

**Demonstrates Same Skills:**
- ✅ Domain-Driven Design (DDD)
- ✅ Hexagonal Architecture (Ports & Adapters)
- ✅ Event-Driven Architecture
- ✅ CQRS Pattern
- ✅ Bounded Context Communication
- ✅ Clean Architecture Principles

**Portfolio Value:**
- Shows senior-level thinking: "Start simple, scale when needed"
- Demonstrates architectural foresight (migration-ready design)
- Easier to demo in interviews (5 minutes vs 20 minutes setup)
- Same patterns, less infrastructure complexity

**Business Goal:**
- $2000+/month portfolio requirement
- Faster time-to-market
- Same architectural maturity demonstrated

---

## Architecture Overview

### Single JVM, Multiple Bounded Contexts

```
┌─────────────────────────────────────────────────────────────┐
│                    E-commerce Monolith                       │
│                    Single Spring Boot App                    │
│                         Port: 8080                           │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌───────────────────┐         ┌───────────────────┐        │
│  │   Order Context   │         │  Product Context  │        │
│  │                   │         │                   │        │
│  │  - Domain Layer   │         │  - Domain Layer   │        │
│  │  - Application    │         │  - Application    │        │
│  │  - Infrastructure │         │  - Infrastructure │        │
│  │  - API            │         │  - API            │        │
│  └───────────────────┘         └───────────────────┘        │
│           │                              │                   │
│           └──────────────────────────────┘                   │
│                        │                                     │
│                        ▼                                     │
│              ┌──────────────────┐                           │
│              │  Spring Events   │                           │
│              │  (In-Memory)     │                           │
│              └──────────────────┘                           │
│                                                               │
│  ┌────────────────────────────────────────────────────┐    │
│  │            Shared Infrastructure                    │    │
│  │  - H2 Database (in-memory)                         │    │
│  │  - Spring Boot (dependency injection)              │    │
│  │  - JPA/Hibernate (persistence)                     │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## Bounded Context Communication

### Synchronous Communication: Order → Product

**Pattern**: Direct method call via ProductPort abstraction

**Monolith Implementation:**
```java
// Order Context (calls)
@Component
public class ProductServiceAdapter implements ProductPort {
    private final ProductFacade productFacade; // Direct injection!
    
    public ProductInfo getProductInfo(ProductId id) {
        // Same JVM - direct method call (microseconds)
        ProductResponse response = productFacade.getProductById(query);
        return toProductInfo(response);
    }
}

// Product Context (receives)
@Component
public class ProductFacade {
    public ProductResponse getProductById(GetProductByIdQuery query) {
        return useCase.execute(query);
    }
}
```

**Microservices Alternative (Future):**
```java
// Just swap ProductServiceAdapter with ProductRestAdapter
@Component
public class ProductRestAdapter implements ProductPort {
    private final RestTemplate restTemplate;
    
    public ProductInfo getProductInfo(ProductId id) {
        // Network HTTP call (milliseconds)
        return restTemplate.getForObject(
            "http://product-service:8081/products/{id}", 
            ProductResponse.class, 
            id.getValue()
        );
    }
}
```

**Key Insight**: PlaceOrderUseCase only knows about `ProductPort` interface - works with BOTH implementations!

---

### Asynchronous Communication: Product → Order

**Pattern**: Domain events via DomainEventPublisher abstraction

**Monolith Implementation:**
```java
// Product Context (publishes)
@Component
public class DomainEventPublisherAdapter implements DomainEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public void publish(ProductDomainEvent event) {
        // In-memory Spring Events (microseconds)
        applicationEventPublisher.publishEvent(event);
    }
}

// Order Context (subscribes)
@Component
public class ProductEventListener {
    @EventListener
    public void onProductPriceUpdated(ProductPriceUpdatedEvent event) {
        logger.info("Product price updated: {}", event.productId());
        // TODO: Update pending orders with new price
    }
    
    @EventListener
    public void onProductDiscontinued(ProductDiscontinuedEvent event) {
        logger.warn("Product discontinued: {}", event.productId());
        // TODO: Cancel pending orders
    }
    
    @EventListener
    public void onStockUpdated(StockUpdatedEvent event) {
        logger.debug("Stock updated for product: {}", event.productId());
        // TODO: Notify customers waiting for restock
    }
}
```

**Microservices Alternative (Future):**
```java
// Just swap Spring Events with Kafka
@Component
public class DomainEventPublisherAdapter implements DomainEventPublisher {
    private final KafkaTemplate<String, ProductDomainEvent> kafkaTemplate;
    
    public void publish(ProductDomainEvent event) {
        // Network message (milliseconds)
        kafkaTemplate.send("product-events", event);
    }
}

@Component
public class ProductEventListener {
    @KafkaListener(topics = "product-events")
    public void onEvent(ProductDomainEvent event) {
        if (event instanceof ProductPriceUpdatedEvent e) {
            // Handle price update
        }
    }
}
```

---

## Event Publishing Pattern

### Use Case Pattern (All 11 Product Use Cases)

```java
@Component
public class CreateProductUseCase {
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;
    
    public ProductResponse execute(CreateProductCommand command) {
        // 1. Load/Create aggregate
        Product product = Product.create(command);
        
        // 2. Save aggregate (domain events collected)
        Product savedProduct = productRepository.save(product);
        
        // 3. Publish domain events (Spring Events in monolith)
        savedProduct.getDomainEvents().forEach(eventPublisher::publish);
        
        // 4. Clear events (prevent duplicate publishing)
        savedProduct.clearDomainEvents();
        
        // 5. Return DTO
        return ProductResponse.fromDomain(savedProduct);
    }
}
```

### Domain Event Management (Product Aggregate)

```java
public class Product extends AggregateRoot<ProductId> {
    private final List<ProductDomainEvent> domainEvents = new ArrayList<>();
    
    public void updatePrice(Money newPrice) {
        // Validate business rules
        if (!canUpdatePrice()) {
            throw new IllegalProductStateException("Cannot update price");
        }
        
        // Update state
        this.price = newPrice;
        
        // Collect event (not published yet!)
        domainEvents.add(new ProductPriceUpdatedEvent(
            this.id, 
            this.price, 
            LocalDateTime.now()
        ));
    }
    
    public List<ProductDomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

---

## Configuration

### Application Properties (Monolith)

```properties
# Monolith Configuration
spring.application.name=E-commerce-Monolith
server.port=8080

# H2 In-Memory Database (single database for both contexts)
spring.datasource.url=jdbc:h2:mem:ecommerce_db
spring.datasource.driverClassName=org.h2.Driver
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Logging (see Spring Events in action)
logging.level.com.mustapha.ecommerce=DEBUG
logging.level.org.springframework.context.event=DEBUG
```

### Microservices Configuration (Future)

**Order Service (8080):**
```yaml
spring:
  application:
    name: order-service
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: order-service
      topics: product-events
```

**Product Service (8081):**
```yaml
spring:
  application:
    name: product-service
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      topic: product-events
```

---

## Running the Application

### Monolith (Current)

```bash
# 1. Build project
mvn clean install

# 2. Run application (single command!)
mvn spring-boot:run

# 3. Access H2 Console
http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:ecommerce_db
- Username: sa
- Password: (empty)

# 4. Test Order API
http://localhost:8080/api/orders

# 5. Test Product API
http://localhost:8080/api/products
```

**That's it!** One command, one port, one database. Interview demo ready in 5 minutes.

### Microservices (Future - 1 Week Migration)

```bash
# 1. Start Kafka
docker-compose up -d kafka

# 2. Start Product Service
cd product-service
mvn spring-boot:run -Dserver.port=8081

# 3. Start Order Service
cd order-service
mvn spring-boot:run -Dserver.port=8080

# 4. Test communication
# Order → Product (HTTP): http://localhost:8080/api/orders
# Product → Order (Kafka): Check logs for event handling
```

**Migration Time**: ~1 week (because abstractions already exist!)

---

## Migration Path to Microservices

### What Changes?

**Step 1: Swap Adapters (15 minutes)**
- Replace `ProductServiceAdapter` with `ProductRestAdapter` in Order context
- Replace `DomainEventPublisherAdapter` (Spring Events → Kafka)
- Replace `ProductEventListener` (@EventListener → @KafkaListener)

**Step 2: Update Configuration (15 minutes)**
- Split `application.properties` into two files
- Add Kafka configuration
- Configure two ports (8080, 8081)

**Step 3: Deploy Separately (30 minutes)**
- Package each context as separate JAR
- Deploy to different servers/containers
- Configure Kafka cluster

**Step 4: Database Separation (1-2 days)**
- Split schema into `order_db` and `product_db`
- Migrate data
- Update JDBC URLs

**Total Migration Time**: ~1 week

### What Doesn't Change?

✅ Domain Layer (Product.java, Order.java, value objects, domain events)  
✅ Application Layer (Use cases, commands, queries)  
✅ Port Interfaces (ProductPort, DomainEventPublisher, ProductRepository)  
✅ Business Logic (validation, calculations, state machines)  
✅ API Controllers (just change base URL)  
✅ Test Suite (just change Spring profiles)

**This is the power of Hexagonal Architecture!**

---

## Abstractions That Enable Migration

### 1. ProductPort (Synchronous)
```java
// Domain port - works for BOTH monolith and microservices
public interface ProductPort {
    ProductInfo getProductInfo(ProductId productId);
    boolean productExists(ProductId productId);
}
```

**Implementations:**
- **Monolith**: `ProductServiceAdapter` (direct call)
- **Microservices**: `ProductRestAdapter` (HTTP call)

### 2. DomainEventPublisher (Asynchronous)
```java
// Domain port - works for BOTH monolith and microservices
public interface DomainEventPublisher {
    void publish(ProductDomainEvent event);
}
```

**Implementations:**
- **Monolith**: `DomainEventPublisherAdapter` (Spring Events)
- **Microservices**: `DomainEventPublisherAdapter` (Kafka)

### 3. ProductRepository (Persistence)
```java
// Domain port - works for BOTH monolith and microservices
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
}
```

**Implementations:**
- **Monolith**: `ProductRepositoryAdapter` (H2 shared DB)
- **Microservices**: `ProductRepositoryAdapter` (PostgreSQL separate DBs)

---

## Message Broker Comparison

### Spring Events (In-Memory) - **CURRENT**
✅ Zero setup  
✅ Fast (microseconds)  
✅ Simple debugging  
✅ Perfect for monolith  
❌ Same JVM only  
❌ No persistence  
❌ No replay  

### RabbitMQ (Network Queue)
✅ Easy to set up  
✅ Message persistence  
✅ Multiple consumers  
✅ Good for microservices  
❌ Slower than in-memory  
❌ No event replay  
❌ Requires RabbitMQ server  

### Kafka (Event Log/Stream)
✅ Event persistence  
✅ Event replay (time travel!)  
✅ High throughput  
✅ Best for event sourcing  
❌ Complex setup  
❌ Overkill for small projects  
❌ Requires Kafka cluster  

**Recommendation for Migration**: Start with Spring Events (monolith), migrate to RabbitMQ (microservices), consider Kafka if you need event sourcing or analytics.

---

## Interview Talking Points

When presenting this project, emphasize:

1. **"I designed a modular monolith with a clear migration path to microservices"**  
   - Shows strategic thinking, not just coding

2. **"I used Hexagonal Architecture with port abstractions to decouple bounded contexts"**  
   - Demonstrates architectural maturity

3. **"ProductPort works with both direct calls and HTTP - just swap the adapter"**  
   - Shows understanding of dependency inversion principle

4. **"I used Spring Events for in-memory async communication, but designed it to swap to Kafka"**  
   - Shows pragmatism + scalability awareness

5. **"Migration time is ~1 week because domain logic is isolated from infrastructure"**  
   - Proves the architecture works

6. **"I chose monolith for faster development, but same DDD patterns as microservices"**  
   - Shows business value awareness

7. **"Event-driven architecture enables loose coupling between Order and Product contexts"**  
   - Demonstrates understanding of reactive patterns

---

## Testing the Architecture

### Test Synchronous Communication (Order → Product)

```bash
# 1. Create a product
POST http://localhost:8080/api/products
{
  "name": "Gaming Laptop",
  "description": "High-performance laptop",
  "price": 1299.99,
  "sku": "LAPTOP-001",
  "totalStock": 10
}

# 2. Place an order (calls ProductServiceAdapter)
POST http://localhost:8080/api/orders
{
  "customerId": "CUST-123",
  "items": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "quantity": 2,
      "unitPrice": 1299.99
    }
  ]
}

# 3. Check logs - you'll see:
# ProductServiceAdapter: Calling ProductFacade.getProductById()
# PlaceOrderUseCase: Product found, placing order
```

### Test Asynchronous Communication (Product → Order)

```bash
# 1. Update product price
PUT http://localhost:8080/api/products/{id}/price
{
  "price": 1199.99
}

# 2. Check logs - you'll see:
# UpdatePriceUseCase: Publishing ProductPriceUpdatedEvent
# DomainEventPublisherAdapter: Publishing event via Spring Events
# ProductEventListener: Product price updated: {id}

# 3. Verify event logging
# Look for: logging.level.org.springframework.context.event=DEBUG
```

---

## Benefits Achieved

**Development Speed:**
- ✅ 2-3 weeks instead of 6-8 weeks
- ✅ No Kafka/Docker/K8s complexity
- ✅ Single `mvn spring-boot:run` command

**Same Learning Value:**
- ✅ DDD (aggregates, value objects, domain events)
- ✅ Hexagonal Architecture (ports, adapters)
- ✅ Event-Driven (async communication)
- ✅ CQRS (commands, queries separated)
- ✅ Bounded Contexts (Order, Product)

**Production-Ready Features:**
- ✅ Optimistic locking (versioning)
- ✅ Idempotency (reservations)
- ✅ Business rule validation
- ✅ Event sourcing-ready
- ✅ Test coverage (unit + integration)

**Portfolio Value:**
- ✅ Shows senior-level architecture skills
- ✅ Demonstrates pragmatism ("start simple, scale when needed")
- ✅ Easy to demo (5-minute setup)
- ✅ Proves migration readiness (Hexagonal Architecture)

---

## Next Steps

1. ✅ **Complete Implementation** (DONE)
   - All 11 Product use cases publish events
   - ProductServiceAdapter for direct calls
   - ProductEventListener for async handling

2. **Test Communication Flows**
   - Create product → Place order (sync)
   - Update price → Event received (async)
   - Discontinue product → Event received (async)

3. **Add Business Logic**
   - ProductEventListener.onProductPriceUpdated: Update pending orders
   - ProductEventListener.onProductDiscontinued: Cancel orders
   - ProductEventListener.onStockUpdated: Notify customers

4. **Document Examples**
   - Add Postman collection
   - Add curl examples
   - Add sequence diagrams

5. **Optional: Prepare Microservices Branch**
   - Create `feature/microservices` branch
   - Implement ProductRestAdapter
   - Add Kafka configuration
   - Document migration process

---

## Conclusion

**This is NOT a "toy project" monolith.**

This is a **strategically designed modular monolith** that:
- Demonstrates enterprise-level architectural skills
- Can migrate to microservices in ~1 week
- Shows pragmatic decision-making (speed vs complexity)
- Proves understanding of Hexagonal Architecture
- Validates DDD and Event-Driven principles

**Portfolio Value**: $2000+/month achievable in 2-3 weeks vs 6-8 weeks with microservices, while demonstrating THE SAME architectural maturity.

**Interview Impact**: "I chose a modular monolith to optimize for time-to-market, but designed it with clear bounded contexts and port abstractions to enable migration to microservices when business needs justify the added complexity."

**That's a senior-level answer.** 🚀
