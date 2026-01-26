package com.mustapha.ecommerce.user.domain.model.valueobject;

/**
 * Value object representing user roles for e-commerce authorization.
 * Used for role-based access control (RBAC) in e-commerce context.
 * 
 * Role Hierarchy:
 * - CUSTOMER: Buys products, manages own orders
 * - EMPLOYEE: Views orders, analytics (read-only for business operations)
 * - OWNER: Full system access (admin privileges)
 */
public enum Role {
    /**
     * Customer role - buys products and manages own account.
     * Can: 
     * - Browse products
     * - Place orders
     * - View own order history
     * - Manage own profile
     * - Update payment methods
     * Cannot:
     * - View other customers' data
     * - Access analytics
     * - Manage products or inventory
     */
    CUSTOMER("ROLE_CUSTOMER"),

    /**
     * Employee role - business operations and analytics.
     * Can:
     * - View all orders (read-only)
     * - View analytics dashboards
     * - View customer data (for support)
     * - Generate reports
     * Cannot:
     * - Modify products or pricing
     * - Refund orders (needs OWNER)
     * - Manage users
     * - Change system configuration
     */
    EMPLOYEE("ROLE_EMPLOYEE"),

    /**
     * Owner role - full system access (administrator).
     * Can:
     * - All EMPLOYEE permissions
     * - Manage products and inventory
     * - Manage users (create employees, block customers)
     * - Process refunds
     * - Change system configuration
     * - View financial reports
     * - Manage promotions and discounts
     */
    OWNER("ROLE_OWNER");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    /**
     * Gets the Spring Security authority string.
     * Required for @PreAuthorize and SecurityContext.
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Checks if this role is OWNER (has admin privileges).
     */
    public boolean isOwner() {
        return this == OWNER;
    }

    /**
     * Checks if this role is EMPLOYEE.
     */
    public boolean isEmployee() {
        return this == EMPLOYEE;
    }

    /**
     * Checks if this role is CUSTOMER.
     */
    public boolean isCustomer() {
        return this == CUSTOMER;
    }

    /**
     * Checks if this role can view analytics.
     * Analytics access: EMPLOYEE + OWNER
     */
    public boolean canViewAnalytics() {
        return this == EMPLOYEE || this == OWNER;
    }

    /**
     * Checks if this role can manage products.
     * Product management: OWNER only
     */
    public boolean canManageProducts() {
        return this == OWNER;
    }

    /**
     * Checks if this role can manage users.
     * User management: OWNER only
     */
    public boolean canManageUsers() {
        return this == OWNER;
    }

    /**
     * Checks if this role can view all orders.
     * All orders visibility: EMPLOYEE + OWNER
     */
    public boolean canViewAllOrders() {
        return this == EMPLOYEE || this == OWNER;
    }

    /**
     * Checks if this role can view customer data (for support purposes).
     * Customer data access: EMPLOYEE + OWNER
     */
    public boolean canViewCustomerData() {
        return this == EMPLOYEE || this == OWNER;
    }

    /**
     * Checks if this role can process refunds.
     * Refund permission: OWNER only
     */
    public boolean canProcessRefunds() {
        return this == OWNER;
    }

    /**
     * Checks if this role can view financial reports.
     * Financial data access: OWNER only (sensitive data)
     */
    public boolean canViewFinancials() {
        return this == OWNER;
    }

    /**
     * Checks if this role can manage promotions and discounts.
     * Promotion management: OWNER only
     */
    public boolean canManagePromotions() {
        return this == OWNER;
    }

    /**
     * Creates a Role from a string (case-insensitive).
     */
    public static Role fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or blank");
        }

        String normalized = value.toUpperCase().replace("ROLE_", "");
        
        try {
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + value);
        }
    }

    @Override
    public String toString() {
        return authority;
    }
}
