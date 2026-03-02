-- V1_3__Create_Inventory_Tables.sql
-- Inventory Management System Schema

-- Main inventory table
CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    available_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    warehouse_id BIGINT,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0, -- Optimistic locking
    CONSTRAINT check_available_quantity CHECK (available_quantity >= 0),
    CONSTRAINT check_reserved_quantity CHECK (reserved_quantity >= 0),
    INDEX idx_product_id (product_id),
    INDEX idx_low_stock (available_quantity)
);

-- Inventory reservations (for cart checkout)
CREATE TABLE inventory_reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    order_id BIGINT,
    customer_id BIGINT,
    status VARCHAR(20) NOT NULL, -- ACTIVE, COMMITTED, RELEASED, EXPIRED
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_order_id (order_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at)
);

-- Inventory transaction log (audit trail)
CREATE TABLE inventory_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity_change INT NOT NULL, -- Positive for additions, negative for deductions
    transaction_type VARCHAR(50) NOT NULL, -- SALE, RETURN, RESTOCK, ADJUSTMENT, RESERVATION, RELEASE
    reference_id BIGINT, -- Order ID, PO number, etc.
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    INDEX idx_product_id (product_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_created_at (created_at)
);
