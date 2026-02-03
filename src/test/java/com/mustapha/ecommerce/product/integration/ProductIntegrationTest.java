package com.mustapha.ecommerce.product.integration;

import static org.assertj.core.api.Assertions.*;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import com.mustapha.ecommerce.product.dto.ProductResponse;
import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.mustapha.ecommerce.product.infrastructure.persistence.repository.SpringDataProductRepository;
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

/**
 * Integration Tests - Product Full Vertical Slice
 * Tests complete flow: HTTP → Controller → Facade → Use Case → Domain → Repository → Database
 * 
 * Pattern: Integration Testing with Real Database (H2 in-memory)
 * Focus: End-to-end validation including persistence
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "spring.cache.type=none"
})
@Transactional
@DisplayName("Product Integration Tests - Full Stack")
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataProductRepository productJpaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordHasher passwordHasher;

    private String adminJwt;

    @BeforeEach
    void setUp() throws Exception {
        productJpaRepository.deleteAll();
        
        User admin = User.create(
            Username.of("productadmin"),
            Email.of("productadmin@example.com"),
            Password.fromPlainText("Admin123!@#", passwordHasher),
            Role.OWNER
        );
        admin.acceptTerms("v1.0");
        admin.verifyEmail();
        admin.activate("Test setup");
        userRepository.save(admin);

        LoginRequest loginRequest = new LoginRequest("productadmin@example.com", "Admin123!@#");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
            loginResult.getResponse().getContentAsString(),
            LoginResponse.class
        );
        adminJwt = loginResponse.getAccessToken();
    }

    @Nested
    @DisplayName("Create and Retrieve Product - Full Flow")
    class CreateAndRetrieveTests {

        @Test
        @DisplayName("Should create product via API and persist to database")
        void shouldCreateProductViaApiAndPersistToDatabase() throws Exception {
            // Arrange
            ProductRequest request = new ProductRequest(
                "LAPTOP-2024",
                "Integration Test Laptop",
                "High performance laptop for testing",
                new BigDecimal("1299.99"),
                "USD",
                50
            );

            // Act - Create product via HTTP POST
            MvcResult result = mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("LAPTOP-2024"))
                .andExpect(jsonPath("$.name").value("Integration Test Laptop"))
                .andExpect(jsonPath("$.price").value(1299.99))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.totalStock").value(50))
                .andExpect(jsonPath("$.availableStock").value(50))
                .andExpect(jsonPath("$.reservedStock").value(0))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

            // Extract product ID from response
            String responseBody = result.getResponse().getContentAsString();
            ProductResponse createdProduct = objectMapper.readValue(responseBody, ProductResponse.class);
            String productId = createdProduct.getId();

            // Assert - Verify database persistence
            ProductJpaEntity savedEntity = productJpaRepository.findById(productId).orElse(null);
            assertThat(savedEntity).isNotNull();
            assertThat(savedEntity.getSku()).isEqualTo("LAPTOP-2024");
            assertThat(savedEntity.getName()).isEqualTo("Integration Test Laptop");
            assertThat(savedEntity.getPrice()).isEqualByComparingTo(new BigDecimal("1299.99"));
            assertThat(savedEntity.getTotalStock()).isEqualTo(50);
            assertThat(savedEntity.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should retrieve product by ID after creation")
        void shouldRetrieveProductByIdAfterCreation() throws Exception {
            // Arrange - Create product first
            ProductRequest request = new ProductRequest(
                "MOUSE-2024",
                "Wireless Mouse",
                "Ergonomic wireless mouse",
                new BigDecimal("29.99"),
                "USD",
                200
            );

            MvcResult createResult = mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

            ProductResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), 
                ProductResponse.class
            );

            // Act - Retrieve by ID
            mockMvc.perform(get("/api/products/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.sku").value("MOUSE-2024"))
                .andExpect(jsonPath("$.name").value("Wireless Mouse"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.totalStock").value(200));
        }

        @Test
        @DisplayName("Should retrieve product by SKU after creation")
        void shouldRetrieveProductBySkuAfterCreation() throws Exception {
            // Arrange - Create product
            ProductRequest request = new ProductRequest(
                "KEYBOARD-2024",
                "Mechanical Keyboard",
                "RGB mechanical keyboard",
                new BigDecimal("89.99"),
                "USD",
                75
            );

            mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            // Act - Retrieve by SKU
            mockMvc.perform(get("/api/products")
                    .param("sku", "KEYBOARD-2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("KEYBOARD-2024"))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.price").value(89.99));
        }

        @Test
        @DisplayName("Should return 404 when product not found by ID")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/products/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Product not found"));
        }
    }

    @Nested
    @DisplayName("Stock Management - Reserve, Release, Fulfill")
    class StockManagementTests {

        private String createTestProduct() throws Exception {
            ProductRequest request = new ProductRequest(
                "STOCK-TEST-001",
                "Stock Test Product",
                "Product for stock testing",
                new BigDecimal("99.99"),
                "USD",
                100
            );

            MvcResult result = mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

            ProductResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProductResponse.class
            );
            return response.getId();
        }

        @Test
        @DisplayName("Should reserve stock and update database")
        void shouldReserveStockAndUpdateDatabase() throws Exception {
            // Arrange
            String productId = createTestProduct();

            // Act - Reserve 10 units for order-001
            mockMvc.perform(post("/api/products/" + productId + "/reserve-stock")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("orderId", "order-001")
                    .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStock").value(100))
                .andExpect(jsonPath("$.availableStock").value(90))
                .andExpect(jsonPath("$.reservedStock").value(10));

            // Assert - Verify database state
            ProductJpaEntity entity = productJpaRepository.findById(productId).orElseThrow();
            assertThat(entity.getTotalStock()).isEqualTo(100);
            assertThat(entity.getAvailableStock()).isEqualTo(90);
            assertThat(entity.getReservedStock()).isEqualTo(10);
            assertThat(entity.getReservations()).containsEntry("order-001", 10);
        }

        @Test
        @DisplayName("Should release reservation and restore available stock")
        void shouldReleaseReservationAndRestoreStock() throws Exception {
            // Arrange - Create product and reserve stock
            String productId = createTestProduct();
            mockMvc.perform(post("/api/products/" + productId + "/reserve-stock")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("orderId", "order-002")
                    .param("quantity", "20"))
                .andExpect(status().isOk());

            // Act - Release reservation
            mockMvc.perform(post("/api/products/" + productId + "/release-reservation")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("orderId", "order-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStock").value(100))
                .andExpect(jsonPath("$.availableStock").value(100))
                .andExpect(jsonPath("$.reservedStock").value(0));

            // Assert - Verify database
            ProductJpaEntity entity = productJpaRepository.findById(productId).orElseThrow();
            assertThat(entity.getAvailableStock()).isEqualTo(100);
            assertThat(entity.getReservedStock()).isEqualTo(0);
            assertThat(entity.getReservations()).doesNotContainKey("order-002");
        }

        @Test
        @DisplayName("Should fulfill reservation and reduce total stock")
        void shouldFulfillReservationAndReduceTotalStock() throws Exception {
            // Arrange - Create product and reserve stock
            String productId = createTestProduct();
            mockMvc.perform(post("/api/products/" + productId + "/reserve-stock")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("orderId", "order-003")
                    .param("quantity", "15"))
                .andExpect(status().isOk());

            // Act - Fulfill reservation
            mockMvc.perform(post("/api/products/" + productId + "/fulfill-reservation")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("orderId", "order-003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStock").value(85))  // Reduced by 15
                .andExpect(jsonPath("$.availableStock").value(85))
                .andExpect(jsonPath("$.reservedStock").value(0));

            // Assert - Verify database
            ProductJpaEntity entity = productJpaRepository.findById(productId).orElseThrow();
            assertThat(entity.getTotalStock()).isEqualTo(85);
            assertThat(entity.getAvailableStock()).isEqualTo(85);
            assertThat(entity.getReservations()).doesNotContainKey("order-003");
        }

        @Test
        @DisplayName("Should handle multiple concurrent reservations")
        void shouldHandleMultipleConcurrentReservations() throws Exception {
            // Arrange
            String productId = createTestProduct();

            // Act - Reserve for multiple orders
            mockMvc.perform(post("/api/products/" + productId + "/reserve-stock")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("orderId", "order-A")
                    .param("quantity", "25"))
                .andExpect(status().isOk());

            mockMvc.perform(post("/api/products/" + productId + "/reserve-stock")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("orderId", "order-B")
                    .param("quantity", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableStock").value(45))  // 100 - 25 - 30
                .andExpect(jsonPath("$.reservedStock").value(55));   // 25 + 30

            // Assert - Database has both reservations
            ProductJpaEntity entity = productJpaRepository.findById(productId).orElseThrow();
            assertThat(entity.getReservations())
                .hasSize(2)
                .containsEntry("order-A", 25)
                .containsEntry("order-B", 30);
        }

        @Test
        @DisplayName("Should return 409 when insufficient stock for reservation")
        void shouldReturn409WhenInsufficientStock() throws Exception {
            // Arrange
            String productId = createTestProduct();  // Has 100 units

            // Act & Assert - Try to reserve more than available
            mockMvc.perform(post("/api/products/" + productId + "/reserve-stock")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("orderId", "order-overbook")
                    .param("quantity", "150"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Insufficient stock"));
        }
    }

    @Nested
    @DisplayName("Product Updates - Price and Details")
    class ProductUpdateTests {

        private String createTestProduct() throws Exception {
            ProductRequest request = new ProductRequest(
                "UPDATE-TEST-001",
                "Update Test Product",
                "Original description",
                new BigDecimal("50.00"),
                "USD",
                100
            );

            MvcResult result = mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

            ProductResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProductResponse.class
            );
            return response.getId();
        }

        @Test
        @DisplayName("Should update product price and persist to database")
        void shouldUpdatePriceAndPersist() throws Exception {
            // Arrange
            String productId = createTestProduct();

            // Act - Update price
            mockMvc.perform(put("/api/products/" + productId + "/price")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("newPrice", "75.99")
                    .param("currencyCode", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(75.99))
                .andExpect(jsonPath("$.currency").value("USD"));

            // Assert - Database updated
            ProductJpaEntity entity = productJpaRepository.findById(productId).orElseThrow();
            assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("75.99"));
        }

        @Test
        @DisplayName("Should update product details and persist to database")
        void shouldUpdateDetailsAndPersist() throws Exception {
            // Arrange
            String productId = createTestProduct();

            // Act - Update name and description
            mockMvc.perform(put("/api/products/" + productId + "/details")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("name", "Updated Product Name")
                    .param("description", "Updated description with more details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product Name"))
                .andExpect(jsonPath("$.description").value("Updated description with more details"));

            // Assert - Database updated
            ProductJpaEntity entity = productJpaRepository.findById(productId).orElseThrow();
            assertThat(entity.getName()).isEqualTo("Updated Product Name");
            assertThat(entity.getDescription()).isEqualTo("Updated description with more details");
        }

        @Test
        @DisplayName("Should reject price update with different currency")
        void shouldRejectPriceUpdateWithDifferentCurrency() throws Exception {
            // Arrange
            String productId = createTestProduct();  // Created with USD

            // Act & Assert - Try to update with EUR
            mockMvc.perform(put("/api/products/" + productId + "/price")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("newPrice", "60.00")
                    .param("currencyCode", "EUR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid request"));
        }
    }

    @Nested
    @DisplayName("Product State Management - Activate, Deactivate, Discontinue")
    class StateManagementTests {

        private String createTestProduct() throws Exception {
            ProductRequest request = new ProductRequest(
                "STATE-TEST-001",
                "State Test Product",
                "Product for state testing",
                new BigDecimal("100.00"),
                "USD",
                50
            );

            MvcResult result = mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

            ProductResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProductResponse.class
            );
            return response.getId();
        }

        @Test
        @DisplayName("Should deactivate product and persist state")
        void shouldDeactivateProductAndPersistState() throws Exception {
            // Arrange
            String productId = createTestProduct();

            // Act - Deactivate product
            mockMvc.perform(post("/api/products/" + productId + "/deactivate")
                    .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.visible").value(true))  // visible unchanged
                .andExpect(jsonPath("$.availableForPurchase").value(false));

            // Assert - Database state updated
            ProductJpaEntity entity = productJpaRepository.findById(productId).orElseThrow();
            assertThat(entity.isActive()).isFalse();
            assertThat(entity.isVisible()).isTrue();
            assertThat(entity.isAvailableForPurchase()).isFalse();
        }

        @Test
        @DisplayName("Should discontinue product and persist terminal state")
        void shouldDiscontinueProductAndPersistState() throws Exception {
            // Arrange
            String productId = createTestProduct();

            // Act - Discontinue product
            mockMvc.perform(post("/api/products/" + productId + "/discontinue")
                    .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discontinued").value(true))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.availableForPurchase").value(false));

            // Assert - Database state updated
            ProductJpaEntity entity = productJpaRepository.findById(productId).orElseThrow();
            assertThat(entity.isDiscontinued()).isTrue();
            assertThat(entity.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should prevent deactivation when stock is reserved")
        void shouldPreventDeactivationWithReservedStock() throws Exception {
            // Arrange - Create product and reserve stock
            String productId = createTestProduct();
            mockMvc.perform(post("/api/products/" + productId + "/reserve-stock")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("orderId", "blocking-order")
                    .param("quantity", "5"))
                .andExpect(status().isOk());

            // Act & Assert - Deactivation should fail
            mockMvc.perform(post("/api/products/" + productId + "/deactivate")
                    .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Invalid product state"));

            // Database should still show active
            ProductJpaEntity entity = productJpaRepository.findById(productId).orElseThrow();
            assertThat(entity.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Validation and Error Handling")
    class ValidationTests {

        @Test
        @DisplayName("Should reject product creation with invalid data")
        void shouldRejectInvalidProductCreation() throws Exception {
            // Arrange - Invalid request (null SKU)
            ProductRequest invalidRequest = new ProductRequest(
                null,  // Invalid
                "Product Name",
                "Description",
                new BigDecimal("10.00"),
                "USD",
                10
            );

            // Act & Assert
            mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Should reject negative price")
        void shouldRejectNegativePrice() throws Exception {
            // Arrange
            ProductRequest invalidRequest = new ProductRequest(
                "INVALID-001",
                "Invalid Product",
                "Description",
                new BigDecimal("-10.00"),  // Invalid
                "USD",
                10
            );

            // Act & Assert
            mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject negative initial stock")
        void shouldRejectNegativeInitialStock() throws Exception {
            // Arrange
            ProductRequest invalidRequest = new ProductRequest(
                "INVALID-002",
                "Invalid Product",
                "Description",
                new BigDecimal("10.00"),
                "USD",
                -5  // Invalid
            );

            // Act & Assert
            mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Database Consistency and Transactions")
    class DatabaseConsistencyTests {

        @Test
        @DisplayName("Should maintain database consistency across multiple operations")
        void shouldMaintainDatabaseConsistency() throws Exception {
            // Arrange - Create product
            ProductRequest request = new ProductRequest(
                "CONSISTENCY-001",
                "Consistency Test",
                "Testing database consistency",
                new BigDecimal("100.00"),
                "USD",
                100
            );

            MvcResult createResult = mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

            ProductResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ProductResponse.class
            );
            String productId = created.getId();

            // Act - Perform multiple operations
            // 1. Reserve stock
            mockMvc.perform(post("/api/products/" + productId + "/reserve-stock")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("orderId", "consistency-order-1")
                    .param("quantity", "20"))
                .andExpect(status().isOk());

            // 2. Update price
            mockMvc.perform(put("/api/products/" + productId + "/price")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("newPrice", "120.00")
                    .param("currencyCode", "USD"))
                .andExpect(status().isOk());

            // 3. Update details
            mockMvc.perform(put("/api/products/" + productId + "/details")
                    .header("Authorization", "Bearer " + adminJwt)
                    .param("name", "Updated Consistency Test")
                    .param("description", "Updated description"))
                .andExpect(status().isOk());

            // Assert - All changes persisted correctly
            ProductJpaEntity finalEntity = productJpaRepository.findById(productId).orElseThrow();
            assertThat(finalEntity.getName()).isEqualTo("Updated Consistency Test");
            assertThat(finalEntity.getPrice()).isEqualByComparingTo(new BigDecimal("120.00"));
            assertThat(finalEntity.getReservedStock()).isEqualTo(20);
            assertThat(finalEntity.getAvailableStock()).isEqualTo(80);
            assertThat(finalEntity.getReservations()).containsKey("consistency-order-1");
        }

        @Test
        @DisplayName("Should verify SKU uniqueness constraint")
        void shouldEnforceSkuUniquenessConstraint() throws Exception {
            // Arrange - Create first product
            ProductRequest request = new ProductRequest(
                "UNIQUE-SKU-001",
                "First Product",
                "Description",
                new BigDecimal("50.00"),
                "USD",
                10
            );

            mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            // Act & Assert - Try to create another product with same SKU
            ProductRequest duplicateRequest = new ProductRequest(
                "UNIQUE-SKU-001",  // Same SKU
                "Second Product",
                "Different description",
                new BigDecimal("60.00"),
                "USD",
                20
            );

            mockMvc.perform(post("/api/products")
                    .header("Authorization", "Bearer " + adminJwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());  // Should fail due to duplicate SKU
        }
    }
}


