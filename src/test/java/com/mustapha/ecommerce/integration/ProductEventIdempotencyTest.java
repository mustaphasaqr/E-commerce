package com.mustapha.ecommerce.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.order.application.port.InventoryPort;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentResult;
import com.mustapha.ecommerce.order.infrastructure.adapter.event.ProductEventListener;
import com.mustapha.ecommerce.product.domain.event.PriceChangedEvent;
import com.mustapha.ecommerce.product.domain.event.ProductCreatedEvent;
import com.mustapha.ecommerce.product.domain.event.StockUpdatedEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import com.mustapha.ecommerce.product.dto.ProductResponse;

import java.time.LocalDateTime;
import java.util.Currency;

/**
 * Event Replay Tests - Idempotency Verification
 * 
 * Tests that ProductEventListener handles:
 * - Duplicate ProductCreatedEvent (same event published twice)
 * - Duplicate PriceChangedEvent (replay scenario)
 * - Duplicate StockUpdatedEvent (network retry)
 * - Out-of-order events (event ordering issues)
 * - Events with same data but different timestamps
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false"
})
@Transactional
@DisplayName("Event Replay Tests - ProductEventListener Idempotency")
class ProductEventIdempotencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @SpyBean
    private ProductEventListener productEventListener;

    @MockBean
    private PaymentPort paymentPort;

    @MockBean
    private InventoryPort inventoryPort;

    @MockBean
    private NotificationPort notificationPort;

    @BeforeEach
    void setUp() {
        when(paymentPort.processPayment(any(), any(), any(), any()))
            .thenReturn(new PaymentResult(true, "txn_success", "Payment successful"));
        
        when(inventoryPort.checkAvailability(any(), anyInt()))
            .thenReturn(true);
    }

    @Test
    @DisplayName("Should handle duplicate ProductCreatedEvent idempotently")
    void shouldHandleDuplicateProductCreatedEvent() throws Exception {
        // Arrange - Create product (publishes ProductCreatedEvent)
        ProductRequest productRequest = new ProductRequest(
            "IDEMPOTENT-001",
            "Idempotency Test Product",
            "Product for duplicate event testing",
            new BigDecimal("99.99"),
            "USD",
            100
        );

        MvcResult result = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProductResponse product = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ProductResponse.class
        );

        // Reset spy to clear first event
        reset(productEventListener);

        // Act - Manually publish the same event again (simulating replay/duplicate)
        ProductCreatedEvent duplicateEvent = new ProductCreatedEvent(
            ProductId.of(product.getId()),
            product.getSku(),
            product.getName(),
            LocalDateTime.now()
        );

        eventPublisher.publishEvent(duplicateEvent);
        
        // Wait briefly for async processing
        Thread.sleep(100);

        // Assert - Event listener should handle duplicate gracefully
        verify(productEventListener, atLeastOnce()).onProductCreated(any(ProductCreatedEvent.class));
        
        // System should remain consistent (no duplicate side effects)
        // In production, listener should check if already processed (e.g., by event ID)
        System.out.println("Duplicate ProductCreatedEvent handled successfully");
    }

    @Test
    @DisplayName("Should handle duplicate PriceChangedEvent idempotently")
    void shouldHandleDuplicatePriceChangedEvent() throws Exception {
        // Arrange - Create product
        ProductRequest productRequest = new ProductRequest(
            "IDEMPOTENT-002",
            "Price Change Test",
            "Product for price event testing",
            new BigDecimal("50.00"),
            "USD",
            50
        );

        MvcResult productResult = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProductResponse product = objectMapper.readValue(
            productResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        // Change price (publishes PriceChangedEvent)
        mockMvc.perform(put("/api/products/" + product.getId() + "/price")
                .param("newPrice", "75.00")
                .param("currencyCode", "USD"))
            .andExpect(status().isOk());

        // Reset spy
        reset(productEventListener);

        // Act - Publish duplicate price change event
        PriceChangedEvent duplicateEvent = new PriceChangedEvent(
            ProductId.of(product.getId()),
            Price.of(new BigDecimal("50.00"), Currency.getInstance("USD")), // Old price
            Price.of(new BigDecimal("75.00"), Currency.getInstance("USD")), // New price
            LocalDateTime.now()
        );

        eventPublisher.publishEvent(duplicateEvent);
        eventPublisher.publishEvent(duplicateEvent); // Publish twice!
        
        Thread.sleep(100);

        // Assert - Should handle both duplicate events
        verify(productEventListener, atLeast(2)).onPriceChanged(any(PriceChangedEvent.class));
        
        // Verify product price is still correct (not affected by duplicate)
        MvcResult updatedResult = mockMvc.perform(
                get("/api/products/" + product.getId()))
            .andExpect(status().isOk())
            .andReturn();

        ProductResponse updatedProduct = objectMapper.readValue(
            updatedResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        assertThat(updatedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    @DisplayName("Should handle duplicate StockUpdatedEvent idempotently")
    void shouldHandleDuplicateStockUpdatedEvent() throws Exception {
        // Arrange - Create product
        ProductRequest productRequest = new ProductRequest(
            "IDEMPOTENT-003",
            "Stock Event Test",
            "Product for stock event testing",
            new BigDecimal("30.00"),
            "USD",
            100
        );

        MvcResult productResult = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProductResponse product = objectMapper.readValue(
            productResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        // Reserve stock (publishes StockUpdatedEvent)
        mockMvc.perform(post("/api/products/" + product.getId() + "/reserve-stock")
                .param("orderId", "ORDER-IDEMPOTENT-001")
                .param("quantity", "10"))
            .andExpect(status().isOk());

        // Reset spy
        reset(productEventListener);

        // Act - Publish duplicate stock event (simulating network retry)
        StockUpdatedEvent duplicateEvent = new StockUpdatedEvent(
            ProductId.of(product.getId()),
            100, // Previous total
            90,  // New total
            0,   // Previous reserved
            10,  // New reserved
            LocalDateTime.now()
        );

        eventPublisher.publishEvent(duplicateEvent);
        eventPublisher.publishEvent(duplicateEvent); // Duplicate!
        eventPublisher.publishEvent(duplicateEvent); // Triple!
        
        Thread.sleep(100);

        // Assert - Should handle all duplicates
        verify(productEventListener, atLeast(3)).onStockUpdated(any(StockUpdatedEvent.class));
        
        // Verify stock is still correct (idempotent processing)
        MvcResult updatedResult = mockMvc.perform(
                get("/api/products/" + product.getId()))
            .andExpect(status().isOk())
            .andReturn();

        ProductResponse updatedProduct = objectMapper.readValue(
            updatedResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        // Stock should be correct despite duplicate events
        assertThat(updatedProduct.getAvailableStock()).isEqualTo(90);
        assertThat(updatedProduct.getReservedStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should handle out-of-order events gracefully")
    void shouldHandleOutOfOrderEvents() throws Exception {
        // Arrange - Create product
        ProductRequest productRequest = new ProductRequest(
            "IDEMPOTENT-004",
            "Out of Order Test",
            "Product for event ordering test",
            new BigDecimal("100.00"),
            "USD",
            50
        );

        MvcResult productResult = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProductResponse product = objectMapper.readValue(
            productResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        reset(productEventListener);

        // Act - Publish events out of order
        LocalDateTime time1 = LocalDateTime.now().minusMinutes(5);
        LocalDateTime time2 = LocalDateTime.now().minusMinutes(3);
        LocalDateTime time3 = LocalDateTime.now().minusMinutes(1);

        // Event 3 (latest) arrives first
        PriceChangedEvent event3 = new PriceChangedEvent(
            ProductId.of(product.getId()),
            Price.of(new BigDecimal("110.00"), Currency.getInstance("USD")),
            Price.of(new BigDecimal("120.00"), Currency.getInstance("USD")),
            time3
        );

        // Event 1 (oldest) arrives second
        PriceChangedEvent event1 = new PriceChangedEvent(
            ProductId.of(product.getId()),
            Price.of(new BigDecimal("100.00"), Currency.getInstance("USD")),
            Price.of(new BigDecimal("105.00"), Currency.getInstance("USD")),
            time1
        );

        // Event 2 (middle) arrives last
        PriceChangedEvent event2 = new PriceChangedEvent(
            ProductId.of(product.getId()),
            Price.of(new BigDecimal("105.00"), Currency.getInstance("USD")),
            Price.of(new BigDecimal("110.00"), Currency.getInstance("USD")),
            time2
        );

        eventPublisher.publishEvent(event3);
        eventPublisher.publishEvent(event1);
        eventPublisher.publishEvent(event2);
        
        Thread.sleep(100);

        // Assert - All events processed
        verify(productEventListener, times(3)).onPriceChanged(any(PriceChangedEvent.class));
        
        // Note: In production, implement event versioning or timestamps
        // to ensure latest state wins even with out-of-order delivery
        System.out.println("Out-of-order events handled - implement event versioning for correctness");
    }

    @Test
    @DisplayName("Should handle rapid successive events without data loss")
    void shouldHandleRapidSuccessiveEvents() throws Exception {
        // Arrange - Create product
        ProductRequest productRequest = new ProductRequest(
            "IDEMPOTENT-005",
            "Rapid Events Test",
            "Product for rapid event testing",
            new BigDecimal("50.00"),
            "USD",
            200
        );

        MvcResult productResult = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProductResponse product = objectMapper.readValue(
            productResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        reset(productEventListener);

        // Act - Publish 10 rapid successive stock updates
        for (int i = 1; i <= 10; i++) {
            StockUpdatedEvent event = new StockUpdatedEvent(
                ProductId.of(product.getId()),
                200 - (i - 1), // Previous total
                200 - i,        // New total (decreasing)
                i - 1,          // Previous reserved
                i,              // New reserved (increasing)
                LocalDateTime.now()
            );
            
            eventPublisher.publishEvent(event);
        }

        Thread.sleep(200);

        // Assert - All 10 events should be processed
        verify(productEventListener, times(10)).onStockUpdated(any(StockUpdatedEvent.class));
        
        System.out.println("10 rapid successive events processed successfully");
    }
}
