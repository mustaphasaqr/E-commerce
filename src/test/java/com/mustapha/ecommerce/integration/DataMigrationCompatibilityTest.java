package com.mustapha.ecommerce.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderItemRequest;
import com.mustapha.ecommerce.order.dto.OrderResponse;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import com.mustapha.ecommerce.product.dto.ProductResponse;
import com.mustapha.ecommerce.user.dto.LoginRequest;
import com.mustapha.ecommerce.user.dto.LoginResponse;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.Password;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import com.mustapha.ecommerce.user.infrastructure.security.BCryptPasswordHasher;

/**
 * Data Migration Tests - Backward Compatibility
 * 
 * Tests that verify:
 * - Old orders (created before stock integration) still work
 * - System handles orders without stock reservations
 * - Migration from old schema to new schema
 * - Backward compatible API responses
 * - Legacy data can be retrieved and processed
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.cache.type=none"
})
@Transactional
@DisplayName("Data Migration Tests - Backward Compatibility")
class DataMigrationCompatibilityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private com.mustapha.ecommerce.order.infrastructure.persistence.repository.SpringDataOrderRepository springDataOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordHasher passwordHasher;

    @MockBean
    private PaymentPort paymentPort;

    @MockBean
    private InventoryPort inventoryPort;

    @MockBean
    private NotificationPort notificationPort;

    private String employeeJwt;

    @BeforeEach
    void setUp() throws Exception {
        // Create and activate EMPLOYEE user for product/order operations
        Email employeeEmail = Email.of("testemployee@example.com");
        if (userRepository.findByEmail(employeeEmail).isEmpty()) {
            User employee = User.create(
                Username.of("testemployee"),
                employeeEmail,
                Password.fromPlainText("Employee123!@#", passwordHasher),
                Role.EMPLOYEE
            );
            employee.acceptTerms("v1.0");
            employee.verifyEmail();
            employee.activate("Test setup");
            userRepository.save(employee);
        }

        // Login to get JWT token
        LoginRequest loginRequest = new LoginRequest("testemployee@example.com", "Employee123!@#");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
            loginResult.getResponse().getContentAsString(),
            LoginResponse.class
        );
        employeeJwt = loginResponse.getAccessToken();

        when(paymentPort.processPayment(any(), any(), any(), any()))
            .thenReturn(new PaymentResult(true, "txn_success", "Payment successful"));
        
        when(inventoryPort.checkAvailability(any(), anyInt()))
            .thenReturn(true);
    }

    @Test
    @DisplayName("Should handle legacy orders created before stock integration feature")
    void shouldHandleLegacyOrdersWithoutStockReservation() {
        // Arrange - Simulate old order (created before stock reservation was implemented)
        // These orders exist in production database but don't have associated stock reservations
        OrderId orderId = OrderId.generate();
        CustomerId customerId = new CustomerId("LEGACY-CUST-001");
        
        List<OrderItem> items = Arrays.asList(
            new OrderItem(
                new ProductId("LEGACY-PROD-001"),
                "Legacy Product",
                2,
                new Money(new BigDecimal("50.00"))
            )
        );

        // Use reconstitute to create order in old state (no stock reservation)
        Order legacyOrder = Order.reconstitute(
            orderId,
            customerId,
            items,
            OrderStatus.CONFIRMED,
            LocalDateTime.now().minusDays(30), // Created 30 days ago
            LocalDateTime.now().minusDays(30),
            null, // No tracking number
            null, // No carrier
            null, // Not delivered
            null, // Not cancelled
            null  // No version (legacy order)
        );

        // Save legacy order
        Order savedOrder = orderRepository.save(legacyOrder);

        // Act - Retrieve legacy order via API
        Order retrievedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        // Assert - Legacy order should work correctly
        assertThat(retrievedOrder.getId()).isEqualTo(orderId);
        assertThat(retrievedOrder.getCustomerId()).isEqualTo(customerId);
        assertThat(retrievedOrder.getItems()).hasSize(1);
        assertThat(retrievedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(retrievedOrder.getTotalAmount().getAmountAsBigDecimal()).isEqualByComparingTo(new BigDecimal("100.00"));
        
        System.out.println("Legacy order (without stock reservation) works correctly");
    }

    @Test
    @DisplayName("Should allow cancellation of legacy orders without affecting stock")
    void shouldCancelLegacyOrdersWithoutStockImpact() {
        // Arrange - Create legacy order
        OrderId orderId = OrderId.generate();
        CustomerId customerId = new CustomerId("LEGACY-CUST-002");
        
        List<OrderItem> items = Arrays.asList(
            new OrderItem(
                new ProductId("LEGACY-PROD-002"),
                "Cancellable Legacy Product",
                3,
                new Money(new BigDecimal("30.00"))
            )
        );

        Order legacyOrder = Order.reconstitute(
            orderId,
            customerId,
            items,
            OrderStatus.CONFIRMED,
            LocalDateTime.now().minusDays(15),
            LocalDateTime.now().minusDays(15),
            null, null, null, null, null
        );

        Order savedOrder = orderRepository.save(legacyOrder);

        // Act - Cancel legacy order
        Order retrievedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
        retrievedOrder.cancel("Customer requested cancellation");
        Order cancelledOrder = orderRepository.save(retrievedOrder);

        // Assert - Order should be cancelled successfully
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelledOrder.getCancellationReason()).isEqualTo("Customer requested cancellation");
        
        // Note: In production, implement compensating transaction to release stock
        // if the order was created with new stock reservation feature
        System.out.println("Legacy order cancelled successfully (no stock reservation to release)");
    }

    @Test
    @DisplayName("Should handle mixed scenario - legacy and new orders coexisting")
    void shouldHandleMixedLegacyAndNewOrders() throws Exception {
        // Arrange - Create a product (for new orders)
        ProductRequest productRequest = new ProductRequest(
            "MIGRATION-001",
            "Migration Test Product",
            "Product for testing mixed legacy/new orders",
            new BigDecimal("75.00"),
            "USD",
            100
        );

        MvcResult productResult = mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + employeeJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProductResponse product = objectMapper.readValue(
            productResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        // 1. Create legacy order (without stock reservation)
        OrderId legacyOrderId = OrderId.generate();
        List<OrderItem> legacyItems = Arrays.asList(
            new OrderItem(
                new ProductId(product.getId()),
                product.getName(),
                2,
                new Money(new BigDecimal("75.00"))
            )
        );

        Order legacyOrder = Order.reconstitute(
            legacyOrderId,
            new CustomerId("MIGRATION-CUST-LEGACY"),
            legacyItems,
            OrderStatus.CONFIRMED,
            LocalDateTime.now().minusDays(60),
            LocalDateTime.now().minusDays(60),
            null, null, null, null, null
        );

        orderRepository.save(legacyOrder);

        // 2. Create new order (with stock reservation via new API)
        OrderRequest newOrderRequest = new OrderRequest();
        newOrderRequest.setCustomerId("MIGRATION-CUST-NEW");
        newOrderRequest.setItems(Arrays.asList(
            new OrderItemRequest(product.getId(), product.getName(), 3, 75.00)
        ));

        MvcResult newOrderResult = mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + employeeJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newOrderRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        OrderResponse newOrder = objectMapper.readValue(
            newOrderResult.getResponse().getContentAsString(),
            OrderResponse.class
        );

        // Assert - Both orders should coexist
        // Legacy order
        Order retrievedLegacy = orderRepository.findById(legacyOrderId).orElseThrow();
        assertThat(retrievedLegacy.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        
        // New order with stock reservation
        assertThat(newOrder.getStatus()).isEqualTo("CONFIRMED");
        
        // Verify product stock was only reduced by new order (3 units)
        MvcResult updatedProductResult = mockMvc.perform(
                get("/api/products/" + product.getId()))
            .andExpect(status().isOk())
            .andReturn();

        ProductResponse updatedProduct = objectMapper.readValue(
            updatedProductResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        assertThat(updatedProduct.getAvailableStock()).isEqualTo(97); // 100 - 3
        assertThat(updatedProduct.getReservedStock()).isEqualTo(3);
        
        System.out.println("Mixed legacy/new orders coexist correctly");
        System.out.println("Legacy order total: " + retrievedLegacy.getTotalAmount());
        System.out.println("New order total: " + newOrder.getTotalAmount());
    }

    @Test
    @DisplayName("Should migrate legacy order to new format when shipped")
    void shouldMigrateLegacyOrderOnStatusChange() {
        // Arrange - Create legacy order in PROCESSING state (ready to ship)
        OrderId orderId = OrderId.generate();
        List<OrderItem> items = Arrays.asList(
            new OrderItem(
                new ProductId("MIGRATION-PROD-002"),
                "Shippable Product",
                1,
                new Money(new BigDecimal("100.00"))
            )
        );

        Order legacyOrder = Order.reconstitute(
            orderId,
            new CustomerId("MIGRATION-CUST-003"),
            items,
            OrderStatus.PROCESSING, // Must be PROCESSING to ship
            LocalDateTime.now().minusDays(5),
            LocalDateTime.now().minusDays(5),
            null, null, null, null, null
        );

        orderRepository.save(legacyOrder);

        // Act - Ship the legacy order (status transition)
        Order retrievedOrder = orderRepository.findById(orderId).orElseThrow();
        retrievedOrder.ship("TRACK-12345", "FedEx");
        Order shippedOrder = orderRepository.save(retrievedOrder);

        // Assert - Legacy order successfully transitioned to SHIPPED
        assertThat(shippedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(shippedOrder.getTrackingNumber()).isEqualTo("TRACK-12345");
        assertThat(shippedOrder.getCarrier()).isEqualTo("FedEx");
        
        // Order maintains data integrity through migration
        assertThat(shippedOrder.getTotalAmount().getAmountAsBigDecimal()).isEqualByComparingTo(new BigDecimal("100.00"));
        
        System.out.println("Legacy order successfully migrated through status change");
    }

    @Test
    @DisplayName("Should handle bulk legacy order retrieval efficiently")
    void shouldHandleBulkLegacyOrderRetrieval() {
        // Arrange - Create 20 legacy orders
        for (int i = 1; i <= 20; i++) {
            OrderId orderId = OrderId.generate();
            List<OrderItem> items = Arrays.asList(
                new OrderItem(
                    new ProductId("BULK-PROD-" + i),
                    "Bulk Product " + i,
                    1,
                    new Money(new BigDecimal("50.00"))
                )
            );

            Order legacyOrder = Order.reconstitute(
                orderId,
                new CustomerId("BULK-CUST-" + i),
                items,
                OrderStatus.CONFIRMED,
                LocalDateTime.now().minusDays(90 - i), // Different ages
                LocalDateTime.now().minusDays(90 - i),
                null, null, null, null, null
            );

            orderRepository.save(legacyOrder);
        }

        // Act - Retrieve all orders (using Spring Data repository)
        List<Order> allOrders = springDataOrderRepository.findAll().stream()
            .map(entity -> orderRepository.findById(new OrderId(entity.getId())).orElseThrow())
            .toList();

        // Assert - All legacy orders retrieved successfully
        assertThat(allOrders).hasSizeGreaterThanOrEqualTo(20);
        
        // Verify all have correct structure
        long legacyOrderCount = allOrders.stream()
            .filter(order -> order.getCustomerId().getValue().startsWith("BULK-CUST-"))
            .count();
        
        assertThat(legacyOrderCount).isEqualTo(20);
        
        System.out.println("Bulk retrieval of " + legacyOrderCount + " legacy orders successful");
    }

    @Test
    @DisplayName("Should verify data integrity after schema migration")
    void shouldVerifyDataIntegrityAfterMigration() {
        // Arrange - Create order with specific values that must be preserved
        OrderId orderId = OrderId.generate();
        BigDecimal precisePrice = new BigDecimal("123.46"); // Money uses 2 decimal places (SCALE=2)
        
        List<OrderItem> items = Arrays.asList(
            new OrderItem(
                new ProductId("PRECISION-PROD-001"),
                "High Precision Product",
                1,
                new Money(precisePrice)
            )
        );

        Order originalOrder = Order.reconstitute(
            orderId,
            new CustomerId("INTEGRITY-CUST-001"),
            items,
            OrderStatus.CONFIRMED,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null, null, null, null, null
        );

        orderRepository.save(originalOrder);

        // Act - Retrieve and verify
        Order retrievedOrder = orderRepository.findById(orderId).orElseThrow();

        // Assert - All data preserved exactly
        assertThat(retrievedOrder.getId()).isEqualTo(orderId);
        assertThat(retrievedOrder.getCustomerId().getValue()).isEqualTo("INTEGRITY-CUST-001");
        assertThat(retrievedOrder.getItems()).hasSize(1);
        assertThat(retrievedOrder.getItems().get(0).getProductId().getValue())
            .isEqualTo("PRECISION-PROD-001");
        
        // Verify BigDecimal precision maintained
        BigDecimal storedPrice = retrievedOrder.getItems().get(0).getPrice().getAmountAsBigDecimal();
        assertThat(storedPrice).isEqualByComparingTo(precisePrice);
        
        // Verify total calculation is correct
        assertThat(retrievedOrder.getTotalAmount().getAmountAsBigDecimal()).isEqualByComparingTo(precisePrice);
        
        System.out.println("Data integrity verified - precision maintained through migration");
    }

    @Test
    @DisplayName("Should handle null/optional fields in legacy orders gracefully")
    void shouldHandleNullFieldsInLegacyOrders() {
        // Arrange - Create legacy order with minimal data (many null fields)
        OrderId orderId = OrderId.generate();
        List<OrderItem> items = Arrays.asList(
            new OrderItem(
                new ProductId("MINIMAL-PROD-001"),
                "Minimal Product",
                1,
                new Money(new BigDecimal("25.00"))
            )
        );

        Order minimalOrder = Order.reconstitute(
            orderId,
            new CustomerId("MINIMAL-CUST-001"),
            items,
            OrderStatus.PENDING, // Different status
            LocalDateTime.now(),
            LocalDateTime.now(),
            null, // No tracking
            null, // No carrier
            null, // No delivery date
            null, // No cancellation reason
            null  // No version (legacy order)
        );

        orderRepository.save(minimalOrder);

        // Act - Retrieve and verify all null fields handled
        Order retrievedOrder = orderRepository.findById(orderId).orElseThrow();

        // Assert - Null fields should not cause errors
        assertThat(retrievedOrder.getTrackingNumber()).isNull();
        assertThat(retrievedOrder.getCarrier()).isNull();
        assertThat(retrievedOrder.getDeliveredAt()).isNull();
        assertThat(retrievedOrder.getCancellationReason()).isNull();
        
        // Order should still be functional
        assertThat(retrievedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(retrievedOrder.getTotalAmount().getAmountAsBigDecimal()).isEqualByComparingTo(new BigDecimal("25.00"));
        
        System.out.println("Legacy order with null fields handled gracefully");
    }
}
