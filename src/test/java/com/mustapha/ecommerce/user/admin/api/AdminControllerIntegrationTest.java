package com.mustapha.ecommerce.user.admin.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.user.admin.dto.*;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.Password;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import com.mustapha.ecommerce.user.infrastructure.security.BCryptPasswordHasher;
import com.mustapha.ecommerce.user.dto.RegisterUserRequest;
import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenGenerator jwtTokenGenerator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordHasher passwordHasher;

    private String ownerUserId;
    private String ownerJwt;
    private String customerUserId;
    private String customerJwt;

    @BeforeEach
    void setUp() {
        User owner = User.create(
            Username.of("adminuser"),
            Email.of("admin@example.com"),
            Password.fromPlainText("Admin123!@#", passwordHasher),
            Role.OWNER
        );
        owner.acceptTerms("v1.0");
        owner.verifyEmail();
        owner.activate("Test setup");
        owner = userRepository.save(owner);
        ownerUserId = owner.getId().getValue().toString();
        ownerJwt = jwtTokenGenerator.generateAccessToken(ownerUserId, "OWNER", "owner-session");

        User customer = User.create(
            Username.of("customeruser"),
            Email.of("customer@example.com"),
            Password.fromPlainText("Customer123!@#", passwordHasher),
            Role.CUSTOMER
        );
        customer.acceptTerms("v1.0");
        customer.verifyEmail();
        customer.activate("Test setup");
        customer = userRepository.save(customer);
        customerUserId = customer.getId().getValue().toString();
        customerJwt = jwtTokenGenerator.generateAccessToken(customerUserId, "CUSTOMER", "customer-session");
    }

    @Test
    void blockUser_AsOwner_Returns200() throws Exception {
        BlockUserRequest request = new BlockUserRequest(customerUserId, "Suspicious activity detected");

        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/block")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(customerUserId))
            .andExpect(jsonPath("$.status").value("BLOCKED"))
            .andExpect(jsonPath("$.username").value("customeruser"))
            .andExpect(jsonPath("$.email").value("customer@example.com"))
            .andExpect(jsonPath("$.role").value("CUSTOMER"))
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void blockUser_AsCustomer_Returns403() throws Exception {
        BlockUserRequest request = new BlockUserRequest(customerUserId, "Attempting unauthorized block");

        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/block")
                .header("Authorization", "Bearer " + customerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void blockUser_WithoutAuthentication_Returns401() throws Exception {
        BlockUserRequest request = new BlockUserRequest(customerUserId, "No auth header");

        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/block")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized()); // 401 for missing authentication
    }

    @Test
    void unblockUser_AsOwner_Returns200() throws Exception {
        // First block the user
        BlockUserRequest blockRequest = new BlockUserRequest(customerUserId, "Test block");
        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/block")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blockRequest)))
            .andExpect(status().isOk());

        // Then unblock
        UnblockUserRequest unblockRequest = new UnblockUserRequest("Issue resolved");

        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/unblock")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unblockRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void activateUser_AsOwner_Returns200() throws Exception {
        // First deactivate the user
        DeactivateUserRequest deactivateRequest = new DeactivateUserRequest("Test deactivation");
        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/deactivate")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deactivateRequest)))
            .andExpect(status().isOk());

        // Then activate
        ActivateUserRequest request = new ActivateUserRequest("Manually activated");

        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/activate")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void deactivateUser_AsOwner_Returns200() throws Exception {
        DeactivateUserRequest request = new DeactivateUserRequest("Account inactive");

        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/deactivate")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void deleteUser_AsOwner_Returns200() throws Exception {
        DeleteUserRequest request = new DeleteUserRequest(customerUserId, "GDPR deletion request");

        mockMvc.perform(delete("/api/v1/admin/users/" + customerUserId)
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deleted").value(true))
            .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void getAllUsers_AsOwner_Returns200WithPagination() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", "Bearer " + ownerJwt)
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users").isArray())
            .andExpect(jsonPath("$.currentPage").value(0))
            .andExpect(jsonPath("$.pageSize").value(20))
            .andExpect(jsonPath("$.totalPages").exists())
            .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void getAllUsers_AsCustomer_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", "Bearer " + customerJwt)
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isForbidden());
    }

    @Test
    void searchUsers_ByEmail_Returns200() throws Exception {
        SearchUsersRequest request = new SearchUsersRequest(
            "customer@example.com",
            null,
            null,
            null,
            0,
            20
        );

        mockMvc.perform(post("/api/v1/admin/users/search")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.users").isArray())
            .andExpect(jsonPath("$.users", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.users[0].id").exists())
            .andExpect(jsonPath("$.users[0].email").value("customer@example.com"))
            .andExpect(jsonPath("$.users[0].username").value("customeruser"))
            .andExpect(jsonPath("$.users[0].role").value("CUSTOMER"))
            .andExpect(jsonPath("$.users[0].status").exists())
            .andExpect(jsonPath("$.currentPage").value(0))
            .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    void searchUsers_ByUsername_Returns200() throws Exception {
        SearchUsersRequest request = new SearchUsersRequest(
            null,
            "customeruser",
            null,
            null,
            0,
            20
        );

        mockMvc.perform(post("/api/v1/admin/users/search")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users").isArray())            .andExpect(jsonPath("$.currentPage").value(0))
            .andExpect(jsonPath("$.pageSize").value(20))
            .andExpect(jsonPath("$.totalPages").exists())
            .andExpect(jsonPath("$.totalElements").exists())            .andExpect(jsonPath("$.users[0].username").value("customeruser"));
    }

    @Test
    void searchUsers_ByStatus_Returns200() throws Exception {
        SearchUsersRequest request = new SearchUsersRequest(
            null,
            null,
            User.UserStatus.ACTIVE,
            null,
            0,
            20
        );

        mockMvc.perform(post("/api/v1/admin/users/search")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users").isArray());
    }

    @Test
    void searchUsers_ByRole_Returns200() throws Exception {
        SearchUsersRequest request = new SearchUsersRequest(
            null,
            null,
            null,
            Role.CUSTOMER,
            0,
            20
        );

        mockMvc.perform(post("/api/v1/admin/users/search")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users").isArray())
            .andExpect(jsonPath("$.users[*].role").value(everyItem(is("CUSTOMER"))));
    }

    @Test
    void adminEndpoints_Authorization_EnforcedConsistently() throws Exception {
        // Test that all admin endpoints require OWNER role
        BlockUserRequest blockRequest = new BlockUserRequest(customerUserId, "Test");

        // Block endpoint
        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/block")
                .header("Authorization", "Bearer " + customerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blockRequest)))
            .andExpect(status().isForbidden());

        // Delete endpoint
        mockMvc.perform(delete("/api/v1/admin/users/" + customerUserId)
                .header("Authorization", "Bearer " + customerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DeleteUserRequest(customerUserId, "Test"))))
            .andExpect(status().isForbidden());

        // List endpoint
        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", "Bearer " + customerJwt))
            .andExpect(status().isForbidden());

        // Search endpoint
        mockMvc.perform(post("/api/v1/admin/users/search")
                .header("Authorization", "Bearer " + customerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SearchUsersRequest(null, null, null, null, 0, 20))))
            .andExpect(status().isForbidden());
    }

    @Test
    void changeUserRole_CustomerToEmployee_Success() throws Exception {
        ChangeUserRoleRequest request = new ChangeUserRoleRequest(Role.EMPLOYEE, "Promotion to employee");

        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/role")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(customerUserId))
            .andExpect(jsonPath("$.role").value("EMPLOYEE"))
            .andExpect(jsonPath("$.username").value("customeruser"));
    }

    @Test
    void changeUserRole_EmployeeToOwner_Success() throws Exception {
        // First change customer to employee
        ChangeUserRoleRequest toEmployeeRequest = new ChangeUserRoleRequest(Role.EMPLOYEE, "Promotion to employee");
        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/role")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toEmployeeRequest)))
            .andExpect(status().isOk());

        // Then change employee to owner
        ChangeUserRoleRequest toOwnerRequest = new ChangeUserRoleRequest(Role.OWNER, "Promotion to owner");
        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/role")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(toOwnerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    void changeUserRole_SameRole_Returns400() throws Exception {
        ChangeUserRoleRequest request = new ChangeUserRoleRequest(Role.CUSTOMER, "No change");

        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/role")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("User already has role")));
    }

    @Test
    void changeUserRole_BlockedUser_Returns400() throws Exception {
        // Block the user first
        BlockUserRequest blockRequest = new BlockUserRequest(customerUserId, "Security issue");
        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/block")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blockRequest)))
            .andExpect(status().isOk());

        // Try to change role
        ChangeUserRoleRequest roleRequest = new ChangeUserRoleRequest(Role.EMPLOYEE, "Attempt");
        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/role")
                .header("Authorization", "Bearer " + ownerJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("blocked")));
    }

    @Test
    void changeUserRole_NonOwnerAttempt_Returns403() throws Exception {
        // Create an employee user
        User employeeUser = User.create(
            Username.of("employeetest"),
            Email.of("employeetest@example.com"),
            Password.fromPlainText("EmployeeTest123!@#", passwordHasher),
            Role.EMPLOYEE
        );
        employeeUser.acceptTerms("v1.0");
        employeeUser.verifyEmail();
        employeeUser.activate("Setup");
        userRepository.save(employeeUser);

        String employeeJwt = jwtTokenGenerator.generateAccessToken(
            employeeUser.getId().getValue().toString(),
            "EMPLOYEE",
            "test-session"
        );

        ChangeUserRoleRequest request = new ChangeUserRoleRequest(Role.OWNER, "Unauthorized attempt");
        mockMvc.perform(post("/api/v1/admin/users/" + customerUserId + "/role")
                .header("Authorization", "Bearer " + employeeJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}



