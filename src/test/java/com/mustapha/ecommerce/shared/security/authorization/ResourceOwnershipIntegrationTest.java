package com.mustapha.ecommerce.shared.security.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;
import com.mustapha.ecommerce.shared.exception.ErrorCode;
import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import com.mustapha.ecommerce.user.infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Resource Ownership Integration Tests")
@Disabled("Resource ownership verification (@VerifyOwnership annotation) not yet implemented for order endpoints. Tests will be enabled once OrderController implements ownership checks.")
class ResourceOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JwtTokenGenerator jwtTokenGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordHasher passwordHasher;

    private String ownerToken;
    private String attackerToken;
    private String ownerUserId;
    private String attackerUserId;
    private String testOrderId;

    @BeforeEach
    void setUp() {
        User owner = createTestUser("owner@example.com", "Owner User");
        User attacker = createTestUser("attacker@example.com", "Attacker User");

        ownerUserId = owner.getId().getValue().toString();
        attackerUserId = attacker.getId().getValue().toString();

        ownerToken = jwtTokenGenerator.generateAccessToken(ownerUserId, owner.getRole().name(), "owner-session");
        attackerToken = jwtTokenGenerator.generateAccessToken(attackerUserId, attacker.getRole().name(), "attacker-session");

        // Create a test order owned by the "owner" user
        Order testOrder = createTestOrder(owner.getId());
        testOrderId = testOrder.getId().getValue().toString();
    }

    @Test
    @DisplayName("Owner should access their own order")
    void ownerAccessesOwnOrder() throws Exception {
        // This test assumes GET /api/orders/{orderId} endpoint exists with @VerifyOwnership
        mockMvc.perform(get("/api/orders/" + testOrderId)
                       .header("Authorization", "Bearer " + ownerToken))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Non-owner should receive 403 when accessing others order")
    void nonOwnerReceives403() throws Exception {
        mockMvc.perform(get("/api/orders/" + testOrderId)
                       .header("Authorization", "Bearer " + attackerToken))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("$.errorCode").value(ErrorCode.AUTHZ_NOT_RESOURCE_OWNER.name()))
               .andExpect(jsonPath("$.message").value(containsString("permission")));
    }

    @Test
    @DisplayName("Unauthenticated user should receive 401")
    void unauthenticatedUserReceives401() throws Exception {
        mockMvc.perform(get("/api/orders/" + testOrderId))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should verify ownership on DELETE requests")
    void verifyOwnershipOnDelete() throws Exception {
        mockMvc.perform(delete("/api/orders/" + testOrderId)
                       .header("Authorization", "Bearer " + attackerToken))
               .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should verify ownership on PUT/PATCH requests")
    void verifyOwnershipOnUpdate() throws Exception {
        String updateJson = "{\"status\":\"CANCELLED\"}";

        mockMvc.perform(put("/api/orders/" + testOrderId)
                       .header("Authorization", "Bearer " + attackerToken)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(updateJson))
               .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should include X-Request-ID in error response")
    void includeRequestIdInErrorResponse() throws Exception {
        mockMvc.perform(get("/api/orders/" + testOrderId)
                       .header("Authorization", "Bearer " + attackerToken))
               .andExpect(status().isForbidden())
               .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    @DisplayName("Should work with custom request ID")
    void workWithCustomRequestId() throws Exception {
        String customRequestId = "integration-test-request-123";

        mockMvc.perform(get("/api/orders/" + testOrderId)
                       .header("Authorization", "Bearer " + attackerToken)
                       .header("X-Request-ID", customRequestId))
               .andExpect(status().isForbidden())
               .andExpect(header().string("X-Request-ID", customRequestId));
    }

    @Test
    @DisplayName("Owner role should bypass ownership checks")
    void ownerRoleBypassesOwnershipChecks() throws Exception {
        User admin = createTestUser("admin@example.com", "Admin User", Role.OWNER);
        String adminToken = jwtTokenGenerator.generateAccessToken(
            admin.getId().getValue().toString(), 
            admin.getRole().name(),
            "admin-session"
        );

        // Note: This depends on whether @VerifyOwnership checks for ADMIN role
        // Modify based on your actual implementation
        mockMvc.perform(get("/api/orders/" + testOrderId)
                       .header("Authorization", "Bearer " + adminToken))
               .andExpect(status().isOk());
    }

    private User createTestUser(String email, String name) {
        return createTestUser(email, name, Role.CUSTOMER);
    }

    private User createTestUser(String email, String name, Role role) {
        User user = User.create(
            Username.of(name.replace(" ", "").toLowerCase()),
            Email.of(email),
            Password.fromPlainText("SecurePassword123!@#", passwordHasher),
            role
        );
        return userRepository.save(user);
    }

    private Order createTestOrder(UserId ownerId) {
        // Create a test order with one item
        CustomerId customerId = new CustomerId(ownerId.getValue().toString());
        ProductId productId = new ProductId("test-product-123");
        Money price = new Money(new BigDecimal("29.99"));
        OrderItem item = new OrderItem(productId, "Test Product", 2, price);
        
        Order order = new OrderBuilder()
            .withCustomerId(customerId)
            .addItem(item)
            .build();
        
        return orderRepository.save(order);
    }
}
