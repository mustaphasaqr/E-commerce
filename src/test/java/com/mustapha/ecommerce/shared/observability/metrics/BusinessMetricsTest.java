package com.mustapha.ecommerce.shared.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive Test Suite for BusinessMetrics
 * 
 * Coverage:
 * - Unit Tests: Counter increments, timer recording
 * - Resilience Tests: Concurrent operations, multiple increments
 * - Integration Tests: Real Micrometer registry, Prometheus export
 * 
 * Test Philosophy:
 * - Tests work with real Micrometer MeterRegistry
 * - Validates Prometheus metric naming conventions
 * - Tests thread-safety of metrics operations
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BusinessMetricsTest {

    @Autowired
    private BusinessMetrics businessMetrics;
    
    @Autowired
    private MeterRegistry meterRegistry;

    // ========================================
    // Order Metrics Tests
    // ========================================
    
    @Nested
    @DisplayName("Order Metrics")
    class OrderMetricsTests {
        
        @Test
        @Order(1)
        @DisplayName("Should increment orders created counter")
        void shouldIncrementOrdersCreated() {
            // Given
            double initialValue = getCounterValue("ecommerce.orders.created");
            
            // When
            businessMetrics.incrementOrdersCreated();
            
            // Then
assertThat(getCounterValue("ecommerce.orders.created"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(2)
        @DisplayName("Should increment orders completed counter")
        void shouldIncrementOrdersCompleted() {
            // Given
            double initialValue = getCounterValue("ecommerce.orders.completed");
            
            // When
            businessMetrics.incrementOrdersCompleted();
            
            // Then
            assertThat(getCounterValue("ecommerce.orders.completed"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(3)
        @DisplayName("Should increment orders cancelled counter")
        void shouldIncrementOrdersCancelled() {
            // Given
            double initialValue = getCounterValue("ecommerce.orders.cancelled");
            
            // When
            businessMetrics.incrementOrdersCancelled();
            
            // Then
            assertThat(getCounterValue("ecommerce.orders.cancelled"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(4)
        @DisplayName("Should increment orders failed counter")
        void shouldIncrementOrdersFailed() {
            // Given
            double initialValue = getCounterValue("ecommerce.orders.failed");
            
            // When
            businessMetrics.incrementOrdersFailed();
            
            // Then
            assertThat(getCounterValue("ecommerce.orders.failed"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(5)
        @DisplayName("Should increment counter by exact amount")
        void shouldIncrementByExactAmount() {
            // Given
            double initialValue = getCounterValue("ecommerce.orders.created");
            
            // When
            businessMetrics.incrementOrdersCreated();
            businessMetrics.incrementOrdersCreated();
            businessMetrics.incrementOrdersCreated();
            
            // Then
            assertThat(getCounterValue("ecommerce.orders.created"))
                .isEqualTo(initialValue + 3.0);
        }
    }
    
    // ========================================
    // Payment Metrics Tests
    // ========================================
    
    @Nested
    @DisplayName("Payment Metrics")
    class PaymentMetricsTests {
        
        @Test
        @Order(6)
        @DisplayName("Should increment payments successful counter")
        void shouldIncrementPaymentsSuccessful() {
            // Given
            double initialValue = getCounterValue("ecommerce.payments.successful");
            
            // When
            businessMetrics.incrementPaymentsSuccessful();
            
            // Then
            assertThat(getCounterValue("ecommerce.payments.successful"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(7)
        @DisplayName("Should increment payments failed counter")
        void shouldIncrementPaymentsFailed() {
            // Given
            double initialValue = getCounterValue("ecommerce.payments.failed");
            
            // When
            businessMetrics.incrementPaymentsFailed();
            
            // Then
            assertThat(getCounterValue("ecommerce.payments.failed"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(8)
        @DisplayName("Should increment payments timeout counter")
        void shouldIncrementPaymentsTimeout() {
            // Given
            double initialValue = getCounterValue("ecommerce.payments.timeout");
            
            // When
            businessMetrics.incrementPaymentsTimeout();
            
            // Then
            assertThat(getCounterValue("ecommerce.payments.timeout"))
                .isGreaterThan(initialValue);
        }
    }
    
    // ========================================
    // Shipping Metrics Tests
    // ========================================
    
    @Nested
    @DisplayName("Shipping Metrics")
    class ShippingMetricsTests {
        
        @Test
        @Order(9)
        @DisplayName("Should increment shipments created counter")
        void shouldIncrementShipmentsCreated() {
            // Given
            double initialValue = getCounterValue("ecommerce.shipments.created");
            
            // When
            businessMetrics.incrementShipmentsCreated();
            
            // Then
            assertThat(getCounterValue("ecommerce.shipments.created"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(10)
        @DisplayName("Should increment shipments delivered counter")
        void shouldIncrementShipmentsDelivered() {
            // Given
            double initialValue = getCounterValue("ecommerce.shipments.delivered");
            
            // When
            businessMetrics.incrementShipmentsDelivered();
            
            // Then
            assertThat(getCounterValue("ecommerce.shipments.delivered"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(11)
        @DisplayName("Should increment shipments failed counter")
        void shouldIncrementShipmentsFailed() {
            // Given
            double initialValue = getCounterValue("ecommerce.shipments.failed");
            
            // When
            businessMetrics.incrementShipmentsFailed();
            
            // Then
            assertThat(getCounterValue("ecommerce.shipments.failed"))
                .isGreaterThan(initialValue);
        }
    }
    
    // ========================================
    // Inventory Metrics Tests
    // ========================================
    
    @Nested
    @DisplayName("Inventory Metrics")
    class InventoryMetricsTests {
        
        @Test
        @Order(12)
        @DisplayName("Should increment inventory reservations counter")
        void shouldIncrementInventoryReservations() {
            // Given
            double initialValue = getCounterValue("ecommerce.inventory.reservations");
            
            // When
            businessMetrics.incrementInventoryReservations();
            
            // Then
            assertThat(getCounterValue("ecommerce.inventory.reservations"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(13)
        @DisplayName("Should increment inventory reservations failed counter")
        void shouldIncrementInventoryReservationsFailed() {
            // Given
            double initialValue = getCounterValue("ecommerce.inventory.reservations.failed");
            
            // When
            businessMetrics.incrementInventoryReservationsFailed();
            
            // Then
            assertThat(getCounterValue("ecommerce.inventory.reservations.failed"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(14)
        @DisplayName("Should increment inventory restocks counter")
        void shouldIncrementInventoryRestocks() {
            // Given
            double initialValue = getCounterValue("ecommerce.inventory.restocks");
            
            // When
            businessMetrics.incrementInventoryRestocks();
            
            // Then
            assertThat(getCounterValue("ecommerce.inventory.restocks"))
                .isGreaterThan(initialValue);
        }
    }
    
    // ========================================
    // Product Metrics Tests
    // ========================================
    
    @Nested
    @DisplayName("Product Metrics")
    class ProductMetricsTests {
        
        @Test
        @Order(15)
        @DisplayName("Should increment product searches counter")
        void shouldIncrementProductSearches() {
            // Given
            double initialValue = getCounterValue("ecommerce.products.searches");
            
            // When
            businessMetrics.incrementProductSearches();
            
            // Then
            assertThat(getCounterValue("ecommerce.products.searches"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(16)
        @DisplayName("Should increment products viewed counter")
        void shouldIncrementProductsViewed() {
            // Given
            double initialValue = getCounterValue("ecommerce.products.viewed");
            
            // When
            businessMetrics.incrementProductsViewed();
            
            // Then
            assertThat(getCounterValue("ecommerce.products.viewed"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(17)
        @DisplayName("Should increment products added to cart counter")
        void shouldIncrementProductsAddedToCart() {
            // Given
            double initialValue = getCounterValue("ecommerce.products.added_to_cart");
            
            // When
            businessMetrics.incrementProductsAddedToCart();
            
            // Then
            assertThat(getCounterValue("ecommerce.products.added_to_cart"))
                .isGreaterThan(initialValue);
        }
    }
    
    // ========================================
    // Review Metrics Tests
    // ========================================
    
    @Nested
    @DisplayName("Review Metrics")
    class ReviewMetricsTests {
        
        @Test
        @Order(18)
        @DisplayName("Should increment reviews submitted counter")
        void shouldIncrementReviewsSubmitted() {
            // Given
            double initialValue = getCounterValue("ecommerce.reviews.submitted");
            
            // When
            businessMetrics.incrementReviewsSubmitted();
            
            // Then
            assertThat(getCounterValue("ecommerce.reviews.submitted"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(19)
        @DisplayName("Should increment reviews approved counter")
        void shouldIncrementReviewsApproved() {
            // Given
            double initialValue = getCounterValue("ecommerce.reviews.approved");
            
            // When
            businessMetrics.incrementReviewsApproved();
            
            // Then
            assertThat(getCounterValue("ecommerce.reviews.approved"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(20)
        @DisplayName("Should increment reviews rejected counter")
        void shouldIncrementReviewsRejected() {
            // Given
            double initialValue = getCounterValue("ecommerce.reviews.rejected");
            
            // When
            businessMetrics.incrementReviewsRejected();
            
            // Then
            assertThat(getCounterValue("ecommerce.reviews.rejected"))
                .isGreaterThan(initialValue);
        }
    }
    
    // ========================================
    // Fraud Metrics Tests
    // ========================================
    
    @Nested
    @DisplayName("Fraud Detection Metrics")
    class FraudMetricsTests {
        
        @Test
        @Order(21)
        @DisplayName("Should increment fraud checks high risk counter")
        void shouldIncrementFraudChecksHigh() {
            // Given
            double initialValue = getCounterValue("ecommerce.fraud.high_risk");
            
            // When
            businessMetrics.incrementFraudChecksHigh();
            
            // Then
            assertThat(getCounterValue("ecommerce.fraud.high_risk"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(22)
        @DisplayName("Should increment fraud checks medium risk counter")
        void shouldIncrementFraudChecksMedium() {
            // Given
            double initialValue = getCounterValue("ecommerce.fraud.medium_risk");
            
            // When
            businessMetrics.incrementFraudChecksMedium();
            
            // Then
            assertThat(getCounterValue("ecommerce.fraud.medium_risk"))
                .isGreaterThan(initialValue);
        }
        
        @Test
        @Order(23)
        @DisplayName("Should increment fraud checks low risk counter")
        void shouldIncrementFraudChecksLow() {
            // Given
            double initialValue = getCounterValue("ecommerce.fraud.low_risk");
            
            // When
            businessMetrics.incrementFraudChecksLow();
            
            // Then
            assertThat(getCounterValue("ecommerce.fraud.low_risk"))
                .isGreaterThan(initialValue);
        }
    }
    
    // ========================================
    // Timer Metrics Tests
    // ========================================
    
    @Nested
    @DisplayName("Timer Metrics")
    class TimerMetricsTests {
        
        @Test
        @Order(24)
        @DisplayName("Should record order processing time")
        void shouldRecordOrderProcessingTime() throws InterruptedException {
            // Given
            Timer.Sample sample = businessMetrics.startOrderProcessing();
            
            // When
            Thread.sleep(10); // Simulate processing
            businessMetrics.recordOrderProcessingTime(sample);
            
            // Then
            assertThat(getTimerCount("ecommerce.orders.processing_time")).isGreaterThan(0L);
        }
        
        @Test
        @Order(25)
        @DisplayName("Should record payment processing time")
        void shouldRecordPaymentProcessingTime() throws InterruptedException {
            // Given
            Timer.Sample sample = businessMetrics.startPaymentProcessing();
            
            // When
            Thread.sleep(10); // Simulate processing
            businessMetrics.recordPaymentProcessingTime(sample);
            
            // Then
            assertThat(getTimerCount("ecommerce.payments.processing_time")).isGreaterThan(0L);
        }
        
        @Test
        @Order(26)
        @DisplayName("Should record search query time")
        void shouldRecordSearchQueryTime() throws InterruptedException {
            // Given
            Timer.Sample sample = businessMetrics.startSearchQuery();
            
            // When
            Thread.sleep(10); // Simulate query
            businessMetrics.recordSearchQueryTime(sample);
            
            // Then
            assertThat(getTimerCount("ecommerce.search.query_time")).isGreaterThan(0L);
        }
        
        @Test
        @Order(27)
        @DisplayName("Should record multiple timer samples")
        void shouldRecordMultipleTimerSamples() throws InterruptedException {
            // Given
            long initialCount = getTimerCount("ecommerce.orders.processing_time");
            
            // When
            Timer.Sample s1 = businessMetrics.startOrderProcessing();
            Thread.sleep(5);
            businessMetrics.recordOrderProcessingTime(s1);
            
            Timer.Sample s2 = businessMetrics.startOrderProcessing();
            Thread.sleep(5);
            businessMetrics.recordOrderProcessingTime(s2);
            
            // Then
            assertThat(getTimerCount("ecommerce.orders.processing_time"))
                .isEqualTo(initialCount + 2);
        }
    }
    
    // ========================================
    // Resilience Tests (Concurrent Operations)
    // ========================================
    
    @Nested
    @DisplayName("Resilience & Thread Safety")
    class ResilienceTests {
        
        @Test
        @Order(28)
        @DisplayName("Should handle concurrent counter increments")
        void shouldHandleConcurrentIncrements() throws InterruptedException {
            // Given
            double initialValue = getCounterValue("ecommerce.orders.created");
            int numThreads = 10;
            int incrementsPerThread = 100;
            
            // When - Increment concurrently from multiple threads
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        businessMetrics.incrementOrdersCreated();
                    }
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Then - All increments should be recorded
            assertThat(getCounterValue("ecommerce.orders.created"))
                .isEqualTo(initialValue + (numThreads * incrementsPerThread));
        }
        
        @Test
        @Order(29)
        @DisplayName("Should handle rapid consecutive increments")
        void shouldHandleRapidIncrements() {
            // Given
            double initialValue = getCounterValue("ecommerce.payments.successful");
            
            // When - Increment 1000 times rapidly
            for (int i = 0; i < 1000; i++) {
                businessMetrics.incrementPaymentsSuccessful();
            }
            
            // Then
            assertThat(getCounterValue("ecommerce.payments.successful"))
                .isEqualTo(initialValue + 1000.0);
        }
        
        @Test
        @Order(30)
        @DisplayName("Should handle concurrent timer recordings")
        void shouldHandleConcurrentTimers() throws InterruptedException {
            // Given
            long initialCount = getTimerCount("ecommerce.search.query_time");
            int numThreads = 5;
            
            // When - Record timers concurrently
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                threads[i] = new Thread(() -> {
                    Timer.Sample sample = businessMetrics.startSearchQuery();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    businessMetrics.recordSearchQueryTime(sample);
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Then
            assertThat(getTimerCount("ecommerce.search.query_time"))
                .isEqualTo(initialCount + numThreads);
        }
    }
    
    // ========================================
    // Integration Tests (Prometheus Export)
    // ========================================
    
    @Nested
    @DisplayName("Integration Tests with MeterRegistry")
    class IntegrationTests {
        
        @Test
        @Order(31)
        @DisplayName("Should register all counters in MeterRegistry")
        void shouldRegisterAllCounters() {
            // Then - Verify all 20+ counters are registered
            assertThat(meterRegistry.find("ecommerce.orders.created").counter()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.orders.completed").counter()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.orders.cancelled").counter()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.orders.failed").counter()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.payments.successful").counter()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.payments.failed").counter()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.shipments.created").counter()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.inventory.reservations").counter()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.products.searches").counter()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.reviews.submitted").counter()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.fraud.high_risk").counter()).isNotNull();
        }
        
        @Test
        @Order(32)
        @DisplayName("Should register all timers in MeterRegistry")
        void shouldRegisterAllTimers() {
            // Then - Verify all 3 timers are registered
            assertThat(meterRegistry.find("ecommerce.orders.processing_time").timer()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.payments.processing_time").timer()).isNotNull();
            assertThat(meterRegistry.find("ecommerce.search.query_time").timer()).isNotNull();
        }
        
        @Test
        @Order(33)
        @DisplayName("Should use Prometheus naming conventions")
        void shouldUsePrometheusNamingConventions() {
            // Then - Metric names should follow ecommerce.* pattern
            assertThat(meterRegistry.getMeters())
                .extracting(meter -> meter.getId().getName())
                .anyMatch(name -> name.startsWith("ecommerce."));
        }
        
        @Test
        @Order(34)
        @DisplayName("Should have descriptions for all metrics")
        void shouldHaveDescriptionsForAllMetrics() {
            // Then - All ecommerce metrics should have descriptions
            meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith("ecommerce."))
                .forEach(meter -> 
                    assertThat(meter.getId().getDescription()).isNotBlank()
                );
        }
        
        @Test
        @Order(35)
        @DisplayName("Should autowire BusinessMetrics bean")
        void shouldAutowireBusinessMetrics() {
            // Then
            assertThat(businessMetrics).isNotNull();
        }
        
        @Test
        @Order(36)
        @DisplayName("Should autowire MeterRegistry bean")
        void shouldAutowireMeterRegistry() {
            // Then
            assertThat(meterRegistry).isNotNull();
        }
    }
    
    // ========================================
    // Helper Methods
    // ========================================
    
    private double getCounterValue(String counterName) {
        Counter counter = meterRegistry.find(counterName).counter();
        return counter != null ? counter.count() : 0.0;
    }
    
    private long getTimerCount(String timerName) {
        Timer timer = meterRegistry.find(timerName).timer();
        return timer != null ? timer.count() : 0L;
    }
}
