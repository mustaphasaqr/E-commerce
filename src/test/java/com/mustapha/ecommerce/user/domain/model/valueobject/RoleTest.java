package com.mustapha.ecommerce.user.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Role Enum Tests")
class RoleTest {

    @Test
    @DisplayName("Should have CUSTOMER role")
    void shouldHaveCustomerRole() {
        Role role = Role.CUSTOMER;

        assertThat(role).isNotNull();
        assertThat(role.name()).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("Should have EMPLOYEE role")
    void shouldHaveEmployeeRole() {
        Role role = Role.EMPLOYEE;

        assertThat(role).isNotNull();
        assertThat(role.name()).isEqualTo("EMPLOYEE");
    }

    @Test
    @DisplayName("Should have OWNER role")
    void shouldHaveOwnerRole() {
        Role role = Role.OWNER;

        assertThat(role).isNotNull();
        assertThat(role.name()).isEqualTo("OWNER");
    }

    @Test
    @DisplayName("Should parse role from string")
    void shouldParseRoleFromString() {
        Role customer = Role.valueOf("CUSTOMER");
        Role employee = Role.valueOf("EMPLOYEE");
        Role owner = Role.valueOf("OWNER");

        assertThat(customer).isEqualTo(Role.CUSTOMER);
        assertThat(employee).isEqualTo(Role.EMPLOYEE);
        assertThat(owner).isEqualTo(Role.OWNER);
    }

    @Test
    @DisplayName("Should throw exception for invalid role")
    void shouldThrowExceptionForInvalidRole() {
        assertThatThrownBy(() -> Role.valueOf("ADMIN"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should have exactly 3 roles")
    void shouldHaveExactlyThreeRoles() {
        Role[] roles = Role.values();

        assertThat(roles).hasSize(3);
        assertThat(roles).contains(Role.CUSTOMER, Role.EMPLOYEE, Role.OWNER);
    }

    @Test
    @DisplayName("Should be comparable")
    void shouldBeComparable() {
        assertThat(Role.CUSTOMER.compareTo(Role.EMPLOYEE)).isLessThan(0);
        assertThat(Role.CUSTOMER.compareTo(Role.CUSTOMER)).isEqualTo(0);
    }
}
