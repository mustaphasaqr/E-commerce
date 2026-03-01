package com.mustapha.ecommerce.order.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.mustapha.ecommerce.order.application.port.ProductPort;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderItemRequest;
import com.mustapha.ecommerce.order.dto.OrderResponse;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.repository.SpringDataOrderRepository;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.Password;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import com.mustapha.ecommerce.user.infrastructure.security.BCryptPasswordHasher;
import com.mustapha.ecommerce.user.dto.LoginRequest;
import com.mustapha.ecommerce.user.dto.LoginResponse;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

/**
 * Integration Tests - Full Vertical Slice
 * Tests complete flow: HTTP → Controller → Facade → Use Case → Domain → Repository → Database
 * 
 * Uses H2 in-memory database for persistence validation
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Order Integration Tests - Full Stack")
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataOrderRepository orderJpaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordHasher passwordHasher;

    private String customerJwt;
    private String authenticatedCustomerId; // Store the authenticated user's ID for ownership verification

    // Mock external systems - not part of integration test scope
    @MockBean
    private PaymentPort paymentPort;

    @MockBean
    private InventoryPort inventoryPort;

    @MockBean
    private NotificationPort notificationPort;

    @MockBean
    private ProductPort productPort;

    @BeforeEach
    void setUp() throws Exception {
        // Clear data
        orderJpaRepository.deleteAll();
        
        // Create and activate CUSTOMER user for order operations
        User customer = User.create(
            Username.of("ordercustomer"),
            Email.of("ordercustomer@example.com"),
            Password.fromPlainText("Customer123!@#", passwordHasher),
            Role.CUSTOMER
        );
        customer.acceptTerms("v1.0");
        customer.verifyEmail();
        customer.activate("Test setup");
        User savedCustomer = userRepository.save(customer);
        
        // Store the authenticated customer ID for use in order requests
        authenticatedCustomerId = savedCustomer.getId().getValue().toString();

        // Login to get JWT token
        LoginRequest loginRequest = new LoginRequest("ordercustomer@example.com", "Customer123!@#");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .header("Authorization", "Bearer " + customerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
            loginResult.getResponse().getContentAsString(),
            LoginResponse.class
        );
        customerJwt = loginResponse.getAccessToken();
        
        // Stub payment to always succeed - using actual PaymentPort interface methods
        when(paymentPort.createCheckout(any(), any(), any(), any()))
            .thenReturn(new PaymentPort.CheckoutResult(
                true, 
                "checkout_test_123", 
                "https://accept.paymob.com/iframe/test",
                3600,
                "Checkout created successfully"
            ));
        
        when(paymentPort.verifyPayment(any()))
            .thenReturn(new PaymentPort.PaymentVerificationResult(
                true,
                "txn_test_success",
                PaymentPort.PaymentStatus.SUCCESS,
                "Payment successful"
            ));
        
        // Stub refund to always succeed - using correct signature (4 parameters)
        when(paymentPort.refundPayment(any(), any(), any(), any()))
            .thenReturn(new PaymentPort.RefundResult(
                true, 
                "refund_test_success", 
                "Refund successful"
            ));
        
        
        // Stub product service - always return true for existence and match prices from test data
        when(productPort.productExists(any())).thenReturn(true);
        when(productPort.getAvailableStock(any())).thenReturn(1000); // Large stock for all products
        when(productPort.isDiscontinued(any())).thenReturn(false); // Not discontinued
        when(productPort.getProductPrice(any())).thenAnswer(invocation -> {
            // Return the price that matches what's in the test data
            // This map matches all product IDs used in the Order integration tests
            var productId = invocation.getArgument(0, com.mustapha.ecommerce.order.domain.model.valueobject.ProductId.class);
            String id = productId.getValue();
            BigDecimal price = switch (id) {
                case "PROD-001" -> new BigDecimal("999.99");
                case "PROD-002" -> new BigDecimal("29.99");
                case "PROD-003" -> new BigDecimal("79.99");
                case "PROD-LIFE-001" -> new BigDecimal("100.0");
                case "PROD-A" -> new BigDecimal("19.99");
                case "PROD-B" -> new BigDecimal("25.50");
                case "PROD-C" -> new BigDecimal("99.99");
                case "PROD-X" -> new BigDecimal("10.0");
                case "PROD-Y" -> new BigDecimal("20.0");
                case "PROD-Z" -> new BigDecimal("30.0");
                case "PROD-P1" -> new BigDecimal("33.33");
                case "PROD-P2" -> new BigDecimal("19.99");
                case "PROD-C1" -> new BigDecimal("50.0");
                case "PROD-C2" -> new BigDecimal("75.0");
                default -> new BigDecimal("100.0");
            };
            return new Money(price);
        });
        // Stub inventory check to always succeed
        when(inventoryPort.checkAvailability(any(), anyInt()))
            .thenReturn(true);
    }

    @Nested
    @DisplayName("Create and Retrieve Order - Full Flow")
    class CreateAndRetrieveTests {

        @Test
        @DisplayName("Should create order via API and persist to database")
        void shouldCreateOrderViaApiAndPersistToDatabase() throws Exception {
            // Arrange
            OrderRequest request = new OrderRequest();
            request.setCustomerId(authenticatedCustomerId);
            request.setItems(Arrays.asList(
                new OrderItemRequest("PROD-001", "Integration Test Laptop", 1, 999.99),
                new OrderItemRequest("PROD-002", "Integration Test Mouse", 2, 29.99)
            ));

            // Act - Create order via HTTP POST
            MvcResult result = mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.customerId").value(authenticatedCustomerId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(1059.97)) // 999.99 + (2 * 29.99)
                // Validate items array structure and data
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                // Validate first item details
                .andExpect(jsonPath("$.items[0].productId").value("PROD-001"))
                .andExpect(jsonPath("$.items[0].productName").value("Integration Test Laptop"))
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.items[0].unitPrice").value(999.99))
                // Validate second item details
                .andExpect(jsonPath("$.items[1].productId").value("PROD-002"))
                .andExpect(jsonPath("$.items[1].productName").value("Integration Test Mouse"))
                .andExpect(jsonPath("$.items[1].quantity").value(2))
                .andExpect(jsonPath("$.items[1].unitPrice").value(29.99))
                .andReturn();

            // Extract orderId from response
            String responseBody = result.getResponse().getContentAsString();
            OrderResponse orderResponse = objectMapper.readValue(responseBody, OrderResponse.class);
            String orderId = orderResponse.getOrderId();

            // Assert - Verify order was persisted to database with complete data
            OrderJpaEntity savedEntity = orderJpaRepository.findById(orderId).orElseThrow();
            assertThat(savedEntity).isNotNull();
            assertThat(savedEntity.getCustomerId()).isEqualTo(authenticatedCustomerId);
            assertThat(savedEntity.getItems()).hasSize(2);
            assertThat(savedEntity.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1059.97"));
            // Validate item details in database
            assertThat(savedEntity.getItems().get(0).getProductId()).isEqualTo("PROD-001");
            assertThat(savedEntity.getItems().get(0).getProductName()).isEqualTo("Integration Test Laptop");
            assertThat(savedEntity.getItems().get(0).getQuantity()).isEqualTo(1);
            assertThat(savedEntity.getItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
            assertThat(savedEntity.getItems().get(1).getProductId()).isEqualTo("PROD-002");
            assertThat(savedEntity.getItems().get(1).getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should retrieve order via API from database")
        void shouldRetrieveOrderViaApiFromDatabase() throws Exception {
            // Arrange - Create order first
            OrderRequest request = new OrderRequest();
            request.setCustomerId(authenticatedCustomerId);
            request.setItems(Arrays.asList(
                new OrderItemRequest("PROD-003", "Keyboard", 1, 79.99)
            ));

            MvcResult createResult = mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

            String createResponseBody = createResult.getResponse().getContentAsString();
            OrderResponse createResponse = objectMapper.readValue(createResponseBody, OrderResponse.class);
            String orderId = createResponse.getOrderId();

            // Act - Retrieve order via HTTP GET
            mockMvc.perform(get("/api/orders/{id}", orderId)
                    .header("Authorization", "Bearer " + customerJwt)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.customerId").value(authenticatedCustomerId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(79.99))
                // Validate complete item data is returned
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value("PROD-003"))
                .andExpect(jsonPath("$.items[0].productName").value("Keyboard"))
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.items[0].unitPrice").value(79.99));
        }

        @Test
        @DisplayName("Should handle complete order lifecycle end-to-end")
        void shouldHandleCompleteOrderLifecycle() throws Exception {
            // Step 1: Create Order
            OrderRequest createRequest = new OrderRequest();
            createRequest.setCustomerId(authenticatedCustomerId);
            createRequest.setItems(Arrays.asList(
                new OrderItemRequest("PROD-LIFE-001", "Lifecycle Product", 1, 100.0)
            ));

            MvcResult createResult = mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn();

            String orderId = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                OrderResponse.class
            ).getOrderId();

            // Step 2: Pay Order
            mockMvc.perform(post("/api/orders/{id}/pay", orderId)
                    .header("Authorization", "Bearer " + customerJwt)
                    .param("paymentMethod", "credit_card")
                    .param("paymentToken", "tok_lifecycle_test")
                    .param("amount", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

            // Step 3: Ship Order (auto-transitions PAID → PROCESSING → SHIPPED)
            mockMvc.perform(post("/api/orders/{id}/ship", orderId)
                    .header("Authorization", "Bearer " + customerJwt)
                    .param("trackingNumber", "TRACK-LIFECYCLE-123")
                    .param("carrier", "FedEx"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                // Validate shipping details are captured
                .andExpect(jsonPath("$.trackingNumber").value("TRACK-LIFECYCLE-123"))
                .andExpect(jsonPath("$.carrier").value("FedEx"));

            // Step 4: Deliver Order
            mockMvc.perform(post("/api/orders/{id}/deliver", orderId)
                    .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.deliveredAt").exists());

            // Step 5: Verify final state in database with complete audit trail
            OrderJpaEntity finalEntity = orderJpaRepository.findById(orderId).orElseThrow();
            assertThat(finalEntity.getStatus().name()).isEqualTo("DELIVERED");
            // Validate complete lifecycle data persisted
            assertThat(finalEntity.getTrackingNumber()).isEqualTo("TRACK-LIFECYCLE-123");
            assertThat(finalEntity.getCarrier()).isEqualTo("FedEx");
            assertThat(finalEntity.getDeliveredAt()).isNotNull();
            assertThat(finalEntity.getCreatedAt()).isNotNull();
            assertThat(finalEntity.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Error Handling - Full Stack")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 404 when order not found in database")
        void shouldReturn404WhenOrderNotFoundInDatabase() throws Exception {
            // Use a valid UUID format that doesn't exist in database
            String nonExistentId = "550e8400-e29b-41d4-a716-446655440000";
            
            // Act & Assert
            mockMvc.perform(get("/api/orders/{id}", nonExistentId)
                    .header("Authorization", "Bearer " + customerJwt)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Order not found"));
        }

        @Test
        @DisplayName("Should validate request and return 400 for invalid data")
        void shouldValidateRequestAndReturn400() throws Exception {
            // Arrange - Invalid request (no customerId)
            OrderRequest invalidRequest = new OrderRequest();
            invalidRequest.setCustomerId(null); // Missing required field
            invalidRequest.setItems(Arrays.asList(
                new OrderItemRequest("PROD-001", "Product", 1, 100.0)
            ));

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"));
        }

        @Test
        @DisplayName("Should validate empty items list")
        void shouldValidateEmptyItemsList() throws Exception {
            // Arrange
            OrderRequest emptyItemsRequest = new OrderRequest();
            emptyItemsRequest.setCustomerId(authenticatedCustomerId);
            emptyItemsRequest.setItems(Arrays.asList()); // Empty items

            // Act & Assert
            mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emptyItemsRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Business Rules Validation - Full Stack")
    class BusinessRulesTests {

        @Test
        @DisplayName("Should enforce order state transitions via API")
        void shouldEnforceOrderStateTransitions() throws Exception {
            // Arrange - Create order
            OrderRequest request = new OrderRequest();
            request.setCustomerId(authenticatedCustomerId);
            request.setItems(Arrays.asList(
                new OrderItemRequest("PROD-001", "Product", 1, 999.99)
            ));

            MvcResult createResult = mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

            String orderId = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                OrderResponse.class
            ).getOrderId();

            // Act & Assert - Try to ship without paying (should fail with domain exception)
            mockMvc.perform(post("/api/orders/{id}/ship", orderId)
                    .header("Authorization", "Bearer " + customerJwt)
                    .param("trackingNumber", "TRACK-123")
                    .param("carrier", "FedEx"))
                .andExpect(status().isConflict()) // Domain InvalidOrderStateException → 409
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Invalid order state"));
        }

        @Test
        @DisplayName("Should calculate correct total amount across all layers")
        void shouldCalculateCorrectTotalAmount() throws Exception {
            // Arrange
            OrderRequest request = new OrderRequest();
            request.setCustomerId(authenticatedCustomerId);
            request.setItems(Arrays.asList(
                new OrderItemRequest("PROD-A", "Product A", 3, 19.99),
                new OrderItemRequest("PROD-B", "Product B", 2, 25.50),
                new OrderItemRequest("PROD-C", "Product C", 1, 99.99)
            ));

            // Expected total: (3 * 19.99) + (2 * 25.50) + (1 * 99.99) = 59.97 + 51.00 + 99.99 = 210.96

            // Act
            MvcResult result = mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(210.96))
                // Validate each item's calculation
                .andExpect(jsonPath("$.items[0].productId").value("PROD-A"))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.items[0].unitPrice").value(19.99))
                .andExpect(jsonPath("$.items[1].productId").value("PROD-B"))
                .andExpect(jsonPath("$.items[1].quantity").value(2))
                .andExpect(jsonPath("$.items[1].unitPrice").value(25.50))
                .andExpect(jsonPath("$.items[2].productId").value("PROD-C"))
                .andExpect(jsonPath("$.items[2].quantity").value(1))
                .andExpect(jsonPath("$.items[2].unitPrice").value(99.99))
                .andReturn();

            String orderId = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                OrderResponse.class
            ).getOrderId();

            // Assert - Verify database has correct total and item details
            OrderJpaEntity entity = orderJpaRepository.findById(orderId).orElseThrow();
            assertThat(entity.getTotalAmount()).isEqualByComparingTo(new BigDecimal("210.96"));
            assertThat(entity.getItems()).hasSize(3);
            // Verify each item persisted with correct data
            assertThat(entity.getItems().get(0).getProductId()).isEqualTo("PROD-A");
            assertThat(entity.getItems().get(0).getQuantity()).isEqualTo(3);
            assertThat(entity.getItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("19.99"));
            assertThat(entity.getItems().get(1).getProductId()).isEqualTo("PROD-B");
            assertThat(entity.getItems().get(1).getQuantity()).isEqualTo(2);
            assertThat(entity.getItems().get(2).getProductId()).isEqualTo("PROD-C");
            assertThat(entity.getItems().get(2).getQuantity()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Data Persistence and Integrity")
    class PersistenceTests {

        @Test
        @DisplayName("Should persist multiple orders for same customer")
        void shouldPersistMultipleOrdersForSameCustomer() throws Exception {
            // Arrange
            String customerId = authenticatedCustomerId;

            // Create first order
            OrderRequest request1 = new OrderRequest();
            request1.setCustomerId(customerId);
            request1.setItems(Arrays.asList(
                new OrderItemRequest("PROD-001", "First Order Product", 1, 999.99)
            ));

            // Create second order
            OrderRequest request2 = new OrderRequest();
            request2.setCustomerId(customerId);
            request2.setItems(Arrays.asList(
                new OrderItemRequest("PROD-002", "Second Order Product", 2, 29.99)
            ));

            // Act - Create both orders
            mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

            mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

            // Assert - Verify both orders exist in database with complete data
            List<OrderJpaEntity> customerOrders = orderJpaRepository.findByCustomerId(customerId);
            assertThat(customerOrders).hasSize(2);
            assertThat(customerOrders)
                .extracting(OrderJpaEntity::getCustomerId)
                .containsOnly(customerId);
            // Validate first order details (filter by product ID to ensure correct order)
            OrderJpaEntity order1 = customerOrders.stream()
                .filter(o -> o.getItems().stream().anyMatch(item -> item.getProductId().equals("PROD-001")))
                .findFirst().orElseThrow();
            assertThat(order1.getItems()).hasSize(1);
            assertThat(order1.getItems().get(0).getProductId()).isEqualTo("PROD-001");
            assertThat(order1.getTotalAmount()).isEqualByComparingTo(new BigDecimal("999.99"));
            // Validate second order details (filter by product ID to ensure correct order)
            OrderJpaEntity order2 = customerOrders.stream()
                .filter(o -> o.getItems().stream().anyMatch(item -> item.getProductId().equals("PROD-002")))
                .findFirst().orElseThrow();
            assertThat(order2.getItems()).hasSize(1);
            assertThat(order2.getItems().get(0).getProductId()).isEqualTo("PROD-002");
            assertThat(order2.getTotalAmount()).isEqualByComparingTo(new BigDecimal("59.98"));
        }

        @Test
        @DisplayName("Should maintain referential integrity between order and items")
        void shouldMaintainReferentialIntegrity() throws Exception {
            // Arrange
            OrderRequest request = new OrderRequest();
            request.setCustomerId(authenticatedCustomerId);
            request.setItems(Arrays.asList(
                new OrderItemRequest("PROD-X", "Product X", 1, 10.0),
                new OrderItemRequest("PROD-Y", "Product Y", 2, 20.0),
                new OrderItemRequest("PROD-Z", "Product Z", 3, 30.0)
            ));

            // Act
            MvcResult result = mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

            String orderId = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                OrderResponse.class
            ).getOrderId();

            // Assert - Verify all items belong to the order with complete details
            OrderJpaEntity entity = orderJpaRepository.findById(orderId).orElseThrow();
            assertThat(entity.getItems()).hasSize(3);
            // Verify items are persisted correctly with all attributes
            assertThat(entity.getItems().get(0).getProductId()).isEqualTo("PROD-X");
            assertThat(entity.getItems().get(0).getProductName()).isEqualTo("Product X");
            assertThat(entity.getItems().get(0).getQuantity()).isEqualTo(1);
            assertThat(entity.getItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("10.0"));
            assertThat(entity.getItems().get(1).getProductId()).isEqualTo("PROD-Y");
            assertThat(entity.getItems().get(1).getQuantity()).isEqualTo(2);
            assertThat(entity.getItems().get(1).getPrice()).isEqualByComparingTo(new BigDecimal("20.0"));
            assertThat(entity.getItems().get(2).getProductId()).isEqualTo("PROD-Z");
            assertThat(entity.getItems().get(2).getQuantity()).isEqualTo(3);
            assertThat(entity.getItems().get(2).getPrice()).isEqualByComparingTo(new BigDecimal("30.0"));
        }

        @Test
        @DisplayName("Should preserve BigDecimal precision through all layers")
        void shouldPreserveBigDecimalPrecision() throws Exception {
            // Arrange - Use prices that test precision
            OrderRequest request = new OrderRequest();
            request.setCustomerId(authenticatedCustomerId);
            request.setItems(Arrays.asList(
                new OrderItemRequest("PROD-P1", "Precision Product 1", 1, 33.33),
                new OrderItemRequest("PROD-P2", "Precision Product 2", 3, 19.99)
            ));

            // Expected: 33.33 + (3 * 19.99) = 33.33 + 59.97 = 93.30

            // Act
            MvcResult result = mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(93.30))
                // Validate precision in response items
                .andExpect(jsonPath("$.items[0].unitPrice").value(33.33))
                .andExpect(jsonPath("$.items[0].quantity").value(1))
                .andExpect(jsonPath("$.items[1].unitPrice").value(19.99))
                .andExpect(jsonPath("$.items[1].quantity").value(3))
                .andReturn();

            String orderId = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                OrderResponse.class
            ).getOrderId();

            // Assert - Database has exact precision
            OrderJpaEntity entity = orderJpaRepository.findById(orderId).orElseThrow();
            assertThat(entity.getTotalAmount()).isEqualByComparingTo(new BigDecimal("93.30"));
            assertThat(entity.getItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("33.33"));
            assertThat(entity.getItems().get(1).getPrice()).isEqualByComparingTo(new BigDecimal("19.99"));
        }
    }

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrencyTests {

        @Test
        @DisplayName("Should handle multiple order creations independently")
        void shouldHandleMultipleOrderCreationsIndependently() throws Exception {
            // Arrange
            OrderRequest request1 = new OrderRequest();
            request1.setCustomerId(authenticatedCustomerId);
            request1.setItems(Arrays.asList(
                new OrderItemRequest("PROD-C1", "Concurrent Product 1", 1, 50.0)
            ));

            OrderRequest request2 = new OrderRequest();
            request2.setCustomerId(authenticatedCustomerId);
            request2.setItems(Arrays.asList(
                new OrderItemRequest("PROD-C2", "Concurrent Product 2", 1, 75.0)
            ));

            // Act - Create orders
            MvcResult result1 = mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andReturn();

            MvcResult result2 = mockMvc.perform(post("/api/orders")
                    .header("Authorization", "Bearer " + customerJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andReturn();

            // Assert - Both orders have unique IDs and correct data
            String orderId1 = objectMapper.readValue(result1.getResponse().getContentAsString(), OrderResponse.class).getOrderId();
            String orderId2 = objectMapper.readValue(result2.getResponse().getContentAsString(), OrderResponse.class).getOrderId();

            assertThat(orderId1).isNotEqualTo(orderId2);

            OrderJpaEntity entity1 = orderJpaRepository.findById(orderId1).orElseThrow();
            OrderJpaEntity entity2 = orderJpaRepository.findById(orderId2).orElseThrow();

            assertThat(entity1.getCustomerId()).isEqualTo(authenticatedCustomerId);
            assertThat(entity2.getCustomerId()).isEqualTo(authenticatedCustomerId);
            // Validate complete order isolation - each has correct items
            assertThat(entity1.getItems()).hasSize(1);
            assertThat(entity1.getItems().get(0).getProductId()).isEqualTo("PROD-C1");
            assertThat(entity1.getItems().get(0).getQuantity()).isEqualTo(1);
            assertThat(entity1.getTotalAmount()).isEqualByComparingTo(new BigDecimal("50.0"));
            assertThat(entity2.getItems()).hasSize(1);
            assertThat(entity2.getItems().get(0).getProductId()).isEqualTo("PROD-C2");
            assertThat(entity2.getItems().get(0).getQuantity()).isEqualTo(1);
            assertThat(entity2.getTotalAmount()).isEqualByComparingTo(new BigDecimal("75.0"));
        }
    }
}
