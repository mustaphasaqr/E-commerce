-- Flyway Migration V1: Initial Schema
-- E-commerce Database Schema
-- Date: 2026-03-04
-- Description: Creates all tables for the e-commerce application

-- =====================================================
-- 1. USERS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(30) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    hashed_password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    block_reason VARCHAR(500),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME,
    deletion_reason VARCHAR(500),
    terms_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    terms_accepted_at DATETIME,
    terms_version VARCHAR(20),
    marketing_consent_given BOOLEAN NOT NULL DEFAULT FALSE,
    marketing_consent_date DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT chk_username_min_length CHECK (LENGTH(username) >= 3),
    CONSTRAINT chk_email_format CHECK (email LIKE '%@%'),
    INDEX idx_user_email (email),
    INDEX idx_user_username (username),
    INDEX idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. PRODUCTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(255) PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(19,4) NOT NULL,
    cost_of_goods DECIMAL(19,4) DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    total_stock INT NOT NULL DEFAULT 0,
    available_stock INT NOT NULL DEFAULT 0,
    reserved_stock INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    available_for_purchase BOOLEAN NOT NULL DEFAULT TRUE,
    discontinued BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT chk_product_price_positive CHECK (price >= 0),
    CONSTRAINT chk_product_stock_positive CHECK (
        total_stock >= 0 AND 
        available_stock >= 0 AND 
        reserved_stock >= 0
    ),
    CONSTRAINT uk_product_sku UNIQUE (sku),
    INDEX idx_product_sku (sku),
    INDEX idx_product_active (active),
    INDEX idx_product_discontinued (discontinued)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. PRODUCT IMAGES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS product_images (
    product_id VARCHAR(255) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    CONSTRAINT fk_product_image FOREIGN KEY (product_id) 
        REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_images (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. CARTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    session_id VARCHAR(255),
    total_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    converted_order_id BIGINT,
    version BIGINT DEFAULT 0,
    INDEX idx_cart_user (user_id),
    INDEX idx_cart_session (session_id),
    INDEX idx_cart_status (status),
    INDEX idx_cart_created (created_at),
    INDEX idx_cart_updated (last_updated_at),
    INDEX idx_cart_converted_order (converted_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. CART ITEMS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) 
        REFERENCES carts(id) ON DELETE CASCADE,
    INDEX idx_cart_item_cart (cart_id),
    INDEX idx_cart_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6. ORDERS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(255) PRIMARY KEY,
    customer_id VARCHAR(255) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    tracking_number VARCHAR(100),
    carrier VARCHAR(100),
    cancellation_reason VARCHAR(500),
    delivered_at DATETIME,
    checkout_id VARCHAR(255),
    transaction_id VARCHAR(255),
    payment_method VARCHAR(50),
    refund_status VARCHAR(50) DEFAULT 'NONE',
    refund_amount DECIMAL(19,2),
    refund_reason VARCHAR(500),
    refunded_at DATETIME,
    version BIGINT DEFAULT 0,
    CONSTRAINT chk_order_total_positive CHECK (total_amount >= 0),
    INDEX idx_order_customer (customer_id),
    INDEX idx_order_status (status),
    INDEX idx_order_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7. ORDER ITEMS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) 
        REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_item_order (order_id),
    INDEX idx_order_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. ORDER CARTS TABLE (Snapshot of cart at order time)
-- =====================================================
CREATE TABLE IF NOT EXISTS order_carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT,
    session_id VARCHAR(255),
    total_amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    last_updated_at DATETIME NOT NULL,
    converted_order_id BIGINT,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_order_cart_order FOREIGN KEY (order_id) 
        REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_cart_order (order_id),
    INDEX idx_order_cart_user (user_id),
    INDEX idx_order_cart_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 9. ORDER CART ITEMS TABLE (Snapshot of cart items)
-- =====================================================
CREATE TABLE IF NOT EXISTS order_cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_order_cart_item_cart FOREIGN KEY (cart_id) 
        REFERENCES order_carts(id) ON DELETE CASCADE,
    INDEX idx_order_cart_item_cart (cart_id),
    INDEX idx_order_cart_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 10. CUSTOMERS TABLE (Denormalized view for analytics)
-- =====================================================
CREATE TABLE IF NOT EXISTS customers (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INITIAL DATA (Optional)
-- =====================================================
-- Uncomment to insert seed data for development/testing
-- INSERT INTO users (id, username, email, hashed_password, role, status, email_verified, terms_accepted, marketing_consent_given, created_at)
-- VALUES ('admin', 'admin', 'admin@ecommerce.com', '$2a$10$...hash...', 'OWNER', 'ACTIVE', TRUE, TRUE, FALSE, NOW());
