package com.mustapha.ecommerce.order.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mustapha.ecommerce.order.application.command.ShipOrderCommand;
import com.mustapha.ecommerce.order.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.application.port.ShippingPort;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;

/**
 * Ship Order Use Case
 * Responsibility: Orchestrate order shipment
 * Pattern: Use Case (Application Service)
 * 
 * Flow:
 * 1. Retrieve order from repository
 * 2. Create shipment with carrier (Aramex) and get tracking number
 * 3. Call order.ship() with tracking info
 * 4. Save & publish events
 * 5. Send shipping notification
 * 
 * Shipping Integration:
 * - Auto-creates shipment with Aramex (or other provider)
 * - Graceful degradation to MANUAL mode if API unavailable
 * - Returns tracking number + label URL
 */
@Component
public class ShipOrderUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ShipOrderUseCase.class);
    
    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;
    private final NotificationPort notificationPort;
    private final ShippingPort shippingPort;

    public ShipOrderUseCase(OrderRepository orderRepository,
                           DomainEventPublisher eventPublisher,
                           NotificationPort notificationPort,
                           ShippingPort shippingPort) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.notificationPort = notificationPort;
        this.shippingPort = shippingPort;
    }

    @Transactional
    public Order execute(ShipOrderCommand command) {
        // Step 1: Retrieve order
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.getOrderId().getValue()));
        
        // Step 2a: Auto-transition PAID → PROCESSING if needed (convenience for users)
        if (order.getStatus() == OrderStatus.PAID) {
            order.startProcessing();
        }
        
        // Step 2b: Create shipment with carrier (Aramex)
        String trackingNumber;
        String carrier;
        
        if (command.getTrackingNumber() != null && !command.getTrackingNumber().isBlank()) {
            // Manual tracking number provided (backward compatibility)
            trackingNumber = command.getTrackingNumber();
            carrier = command.getCarrier() != null ? command.getCarrier() : "MANUAL";
            logger.info("📋 Using manual tracking: orderId={}, tracking={}", 
                       order.getId().getValue(), trackingNumber);
        } else {
            // Auto-create shipment with carrier
            logger.info("📦 Creating shipment with carrier: orderId={}", order.getId().getValue());
            
            // ============================================
            // PRODUCTION TODO #1: Add delivery address to Order model
            // ============================================
            // Current: Using placeholder address values (hardcoded)
            // Required: Store customer's delivery address in Order during placement
            // Files to update:
            //   1. Order.java - Add deliveryFullName, deliveryAddressLine1, deliveryCity, etc.
            //   2. OrderJpaEntity.java - Add corresponding @Column fields
            //   3. Create database migration (ALTER TABLE orders ADD COLUMN...)
            //   4. OrderRequest DTO - Accept DeliveryAddress in API
            //   5. PlaceOrderCommand - Include DeliveryAddress
            // Reference: See SHIPPING_TODO.md Section 1 for complete implementation guide
            // ============================================
            
            // ============================================
            // PRODUCTION TODO #2: Calculate package weight from OrderItems
            // ============================================
            // Current: Fixed 2.0 kg for all orders
            // Required: Add weightKg to OrderItem or Product, calculate total
            // Files to update:
            //   1. OrderItem.java - Add weightKg field
            //   2. Order.java - Add calculateTotalWeight() method
            //   3. Database migration - Add weight_kg column
            // Reference: See SHIPPING_TODO.md Section 2
            // ============================================
            
            // ============================================
            // PRODUCTION TODO #3: Get customer contact information
            // ============================================
            // Current: "Customer Name", "+201234567890" (placeholders)
            // Required: Load from Customer entity or store in Order
            // Option A: Create Customer aggregate + repository (proper DDD)
            // Option B: Denormalize - store customerName/phone in Order
            // Reference: See SHIPPING_TODO.md Section 3
            // ============================================
            
            // ============================================
            // PRODUCTION TODO #4: Make package dimensions configurable
            // ============================================
            // Current: Fixed 30x20x15 cm
            // Required: Load from application.properties or calculate from products
            // Files to update:
            //   1. application.properties - Add shipping.default-package-* properties
            //   2. Create PackageConfig @ConfigurationProperties
            //   3. Inject PackageConfig into this use case
            // Reference: See SHIPPING_TODO.md Section 4
            // ============================================
            
            var shipmentRequest = new ShippingPort.ShipmentRequest(
                order.getId(),
                
                // TODO #3: Replace with order.getDeliveryFullName() or customer.getFullName()
                "Customer Name", // PLACEHOLDER
                
                // Company (optional)
                "",
                
                // TODO #1: Replace with order.getDeliveryAddressLine1()
                "123 Main Street", // PLACEHOLDER
                
                // Address line 2 (optional)
                "",
                
                // TODO #1: Replace with order.getDeliveryCity()
                "Cairo", // PLACEHOLDER
                
                // State/Province
                "Cairo Governorate", // PLACEHOLDER
                
                // TODO #1: Replace with order.getDeliveryPostalCode()
                "11511", // PLACEHOLDER
                
                // TODO #1: Replace with order.getDeliveryCountry() (EG, AE, SA, etc.)
                "EG", // PLACEHOLDER - Egypt
                
                // TODO #3: Replace with order.getDeliveryPhone() or customer.getPhone()
                "+201234567890", // PLACEHOLDER
                
                // Email (optional)
                "",
                
                // TODO #2: Replace with order.calculateTotalWeight()
                2.0, // PLACEHOLDER - Fixed 2.0 kg
                
                // TODO #4: Replace with packageConfig.getDefaultPackageLength()
                30.0, // PLACEHOLDER - Length in cm
                
                // TODO #4: Replace with packageConfig.getDefaultPackageWidth()
                20.0, // PLACEHOLDER - Width in cm
                
                // TODO #4: Replace with packageConfig.getDefaultPackageHeight()
                15.0, // PLACEHOLDER - Height in cm
                
                // Package description
                "E-commerce order #" + order.getId().getValue(),
                
                // Declared value (for insurance)
                order.getTotalAmount().getAmount(),
                
                // TODO #5 (Optional): Replace with determineServiceType(order)
                // Could be based on: order value, customer tier, delivery urgency
                "EXPRESS" // PLACEHOLDER - Service type (EXPRESS, STANDARD, ECONOMY)
            );
            
            var shipmentResult = shippingPort.createShipment(shipmentRequest);
            
            trackingNumber = shipmentResult.trackingNumber();
            carrier = shipmentResult.carrier();
            
            if (shipmentResult.success()) {
                logger.info("✅ Shipment created: orderId={}, tracking={}, carrier={}", 
                           order.getId().getValue(), trackingNumber, carrier);
                if (shipmentResult.labelUrl() != null) {
                    logger.info("📄 Shipping label: {}", shipmentResult.labelUrl());
                }
            } else {
                logger.warn("⚠️ Shipment creation failed, using manual mode: {}", 
                           shipmentResult.message());
            }
        }
        
        // Step 3: Call domain method (domain validates state transition and stores shipping info)
        order.ship(trackingNumber, carrier);
        
        // Step 4: Save & publish events
        Order savedOrder = orderRepository.save(order);
        savedOrder.getDomainEvents().forEach(eventPublisher::publish);
        savedOrder.clearDomainEvents();
        
        // Step 5: Send notification
        notificationPort.sendOrderShipped(
            order.getCustomerId(),
            order.getId(),
            trackingNumber,
            carrier
        );
        
        return savedOrder;
    }
}
