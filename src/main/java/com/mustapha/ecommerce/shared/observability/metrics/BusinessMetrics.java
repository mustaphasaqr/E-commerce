package com.mustapha.ecommerce.shared.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Business metrics for monitoring
 * Exposed via Prometheus /actuator/prometheus endpoint
 */
@Slf4j
@Component
public class BusinessMetrics {

    private final Counter ordersCreated;
    private final Counter ordersCompleted;
    private final Counter ordersCancelled;
    private final Counter ordersFailed;
    
    private final Counter paymentsSuccessful;
    private final Counter paymentsFailed;
    private final Counter paymentsTimeout;
    
    private final Counter shipmentsCreated;
    private final Counter shipmentsDelivered;
    private final Counter shipmentsFailed;
    
    private final Counter inventoryReservations;
    private final Counter inventoryReservationsFailed;
    private final Counter inventoryRestocks;
    
    private final Counter productSearches;
    private final Counter productsViewed;
    private final Counter productsAddedToCart;
    
    private final Counter reviewsSubmitted;
    private final Counter reviewsApproved;
    private final Counter reviewsRejected;
    
    private final Counter fraudChecksHigh;
    private final Counter fraudChecksMedium;
    private final Counter fraudChecksLow;
    
    private final Timer orderProcessingTime;
    private final Timer paymentProcessingTime;
    private final Timer searchQueryTime;

    public BusinessMetrics(MeterRegistry registry) {
        // Order metrics
        this.ordersCreated = Counter.builder("ecommerce.orders.created")
            .description("Total orders created")
            .register(registry);
        
        this.ordersCompleted = Counter.builder("ecommerce.orders.completed")
            .description("Total orders completed")
            .register(registry);
        
        this.ordersCancelled = Counter.builder("ecommerce.orders.cancelled")
            .description("Total orders cancelled")
            .register(registry);
        
        this.ordersFailed = Counter.builder("ecommerce.orders.failed")
            .description("Total orders failed")
            .register(registry);

        // Payment metrics
        this.paymentsSuccessful = Counter.builder("ecommerce.payments.successful")
            .description("Total successful payments")
            .register(registry);
        
        this.paymentsFailed = Counter.builder("ecommerce.payments.failed")
            .description("Total failed payments")
            .register(registry);
        
        this.paymentsTimeout = Counter.builder("ecommerce.payments.timeout")
            .description("Total payment timeouts")
            .register(registry);

        // Shipping metrics
        this.shipmentsCreated = Counter.builder("ecommerce.shipments.created")
            .description("Total shipments created")
            .register(registry);
        
        this.shipmentsDelivered = Counter.builder("ecommerce.shipments.delivered")
            .description("Total shipments delivered")
            .register(registry);
        
        this.shipmentsFailed = Counter.builder("ecommerce.shipments.failed")
            .description("Total shipment failures")
            .register(registry);

        // Inventory metrics
        this.inventoryReservations = Counter.builder("ecommerce.inventory.reservations")
            .description("Total inventory reservations")
            .register(registry);
        
        this.inventoryReservationsFailed = Counter.builder("ecommerce.inventory.reservations.failed")
            .description("Total failed inventory reservations")
            .register(registry);
        
        this.inventoryRestocks = Counter.builder("ecommerce.inventory.restocks")
            .description("Total inventory restocks")
            .register(registry);

        // Product metrics
        this.productSearches = Counter.builder("ecommerce.products.searches")
            .description("Total product searches")
            .register(registry);
        
        this.productsViewed = Counter.builder("ecommerce.products.viewed")
            .description("Total product views")
            .register(registry);
        
        this.productsAddedToCart = Counter.builder("ecommerce.products.added_to_cart")
            .description("Total products added to cart")
            .register(registry);

        // Review metrics
        this.reviewsSubmitted = Counter.builder("ecommerce.reviews.submitted")
            .description("Total reviews submitted")
            .register(registry);
        
        this.reviewsApproved = Counter.builder("ecommerce.reviews.approved")
            .description("Total reviews approved")
            .register(registry);
        
        this.reviewsRejected = Counter.builder("ecommerce.reviews.rejected")
            .description("Total reviews rejected")
            .register(registry);

        // Fraud detection metrics
        this.fraudChecksHigh = Counter.builder("ecommerce.fraud.high_risk")
            .description("Total high-risk fraud assessments")
            .register(registry);
        
        this.fraudChecksMedium = Counter.builder("ecommerce.fraud.medium_risk")
            .description("Total medium-risk fraud assessments")
            .register(registry);
        
        this.fraudChecksLow = Counter.builder("ecommerce.fraud.low_risk")
            .description("Total low-risk fraud assessments")
            .register(registry);

        // Timing metrics
        this.orderProcessingTime = Timer.builder("ecommerce.orders.processing_time")
            .description("Order processing time")
            .register(registry);
        
        this.paymentProcessingTime = Timer.builder("ecommerce.payments.processing_time")
            .description("Payment processing time")
            .register(registry);
        
        this.searchQueryTime = Timer.builder("ecommerce.search.query_time")
            .description("Search query execution time")
            .register(registry);

        log.info("📊 Business metrics initialized and exposed on /actuator/prometheus");
    }

    // Order metrics
    public void incrementOrdersCreated() { ordersCreated.increment(); }
    public void incrementOrdersCompleted() { ordersCompleted.increment(); }
    public void incrementOrdersCancelled() { ordersCancelled.increment(); }
    public void incrementOrdersFailed() { ordersFailed.increment(); }

    // Payment metrics
    public void incrementPaymentsSuccessful() { paymentsSuccessful.increment(); }
    public void incrementPaymentsFailed() { paymentsFailed.increment(); }
    public void incrementPaymentsTimeout() { paymentsTimeout.increment(); }

    // Shipping metrics
    public void incrementShipmentsCreated() { shipmentsCreated.increment(); }
    public void incrementShipmentsDelivered() { shipmentsDelivered.increment(); }
    public void incrementShipmentsFailed() { shipmentsFailed.increment(); }

    // Inventory metrics
    public void incrementInventoryReservations() { inventoryReservations.increment(); }
    public void incrementInventoryReservationsFailed() { inventoryReservationsFailed.increment(); }
    public void incrementInventoryRestocks() { inventoryRestocks.increment(); }

    // Product metrics
    public void incrementProductSearches() { productSearches.increment(); }
    public void incrementProductsViewed() { productsViewed.increment(); }
    public void incrementProductsAddedToCart() { productsAddedToCart.increment(); }

    // Review metrics
    public void incrementReviewsSubmitted() { reviewsSubmitted.increment(); }
    public void incrementReviewsApproved() { reviewsApproved.increment(); }
    public void incrementReviewsRejected() { reviewsRejected.increment(); }

    // Fraud metrics
    public void incrementFraudChecksHigh() { fraudChecksHigh.increment(); }
    public void incrementFraudChecksMedium() { fraudChecksMedium.increment(); }
    public void incrementFraudChecksLow() { fraudChecksLow.increment(); }

    // Timing metrics
    public Timer.Sample startOrderProcessing() { return Timer.start(); }
    public void recordOrderProcessingTime(Timer.Sample sample) { sample.stop(orderProcessingTime); }
    
    public Timer.Sample startPaymentProcessing() { return Timer.start(); }
    public void recordPaymentProcessingTime(Timer.Sample sample) { sample.stop(paymentProcessingTime); }
    
    public Timer.Sample startSearchQuery() { return Timer.start(); }
    public void recordSearchQueryTime(Timer.Sample sample) { sample.stop(searchQueryTime); }
}
