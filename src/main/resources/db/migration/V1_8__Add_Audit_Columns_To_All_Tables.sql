-- Flyway Migration V1_8: Add Audit Columns To All Tables
-- E-commerce Database Schema - Audit Trail Enhancement
-- Date: 2026-03-04
-- Description: Adds audit columns (created_by, created_at, updated_by, updated_at) to all tables
--              for compliance (GDPR, SOX), fraud prevention, and customer dispute resolution

-- =====================================================
-- STRATEGY:
-- - Tables with existing created_at/updated_at: Add created_by/updated_by
-- - Tables without timestamps: Add all 4 columns
-- - Default created_by/updated_by to 'SYSTEM' for existing data
-- =====================================================

-- =====================================================
-- 1. USERS TABLE (already has created_at/updated_at)
-- =====================================================
ALTER TABLE users
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' AFTER username,
    ADD COLUMN updated_by VARCHAR(100) DEFAULT 'SYSTEM' AFTER updated_at;

-- Remove DEFAULT constraint after backfilling existing data
ALTER TABLE users
    ALTER COLUMN created_by DROP DEFAULT;

-- =====================================================
-- 2. PRODUCTS TABLE (already has created_at/updated_at)
-- =====================================================
ALTER TABLE products
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' AFTER sku,
    ADD COLUMN updated_by VARCHAR(100) DEFAULT 'SYSTEM' AFTER updated_at;

ALTER TABLE products
    ALTER COLUMN created_by DROP DEFAULT;

-- =====================================================
-- 3. CARTS TABLE (already has created_at)
-- =====================================================
ALTER TABLE carts
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' AFTER id,
    ADD COLUMN updated_by VARCHAR(100) DEFAULT 'SYSTEM' AFTER last_updated_at,
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER updated_by;

ALTER TABLE carts
    ALTER COLUMN created_by DROP DEFAULT;

-- =====================================================
-- 4. CART ITEMS TABLE (no audit columns)
-- =====================================================
ALTER TABLE cart_items
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' AFTER id,
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) DEFAULT 'SYSTEM' AFTER price,
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER updated_by;

ALTER TABLE cart_items
    ALTER COLUMN created_by DROP DEFAULT,
    ALTER COLUMN created_at DROP DEFAULT;

-- =====================================================
-- 5. ORDERS TABLE (already has created_at/updated_at)
-- =====================================================
ALTER TABLE orders
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' AFTER customer_id,
    ADD COLUMN updated_by VARCHAR(100) DEFAULT 'SYSTEM' AFTER updated_at;

ALTER TABLE orders
    ALTER COLUMN created_by DROP DEFAULT;

-- =====================================================
-- 6. ORDER ITEMS TABLE (no audit columns)
-- =====================================================
ALTER TABLE order_items
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' AFTER id,
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) DEFAULT 'SYSTEM' AFTER unit_price,
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER updated_by;

ALTER TABLE order_items
    ALTER COLUMN created_by DROP DEFAULT,
    ALTER COLUMN created_at DROP DEFAULT;

-- =====================================================
-- 7. ORDER CARTS TABLE (already has created_at)
-- =====================================================
ALTER TABLE order_carts
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' AFTER id,
    ADD COLUMN updated_by VARCHAR(100) DEFAULT 'SYSTEM' AFTER last_updated_at,
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER updated_by;

ALTER TABLE order_carts
    ALTER COLUMN created_by DROP DEFAULT;

-- =====================================================
-- 8. ORDER CART ITEMS TABLE (no audit columns)
-- =====================================================
ALTER TABLE order_cart_items
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' AFTER id,
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER created_by,
    ADD COLUMN updated_by VARCHAR(100) DEFAULT 'SYSTEM' AFTER price,
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER updated_by;

ALTER TABLE order_cart_items
    ALTER COLUMN created_by DROP DEFAULT,
    ALTER COLUMN created_at DROP DEFAULT;

-- =====================================================
-- 9. CUSTOMERS TABLE (already has created_at)
-- =====================================================
ALTER TABLE customers
    ADD COLUMN created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' AFTER id,
    ADD COLUMN updated_by VARCHAR(100) DEFAULT 'SYSTEM' AFTER created_at,
    ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER updated_by;

ALTER TABLE customers
    ALTER COLUMN created_by DROP DEFAULT;

-- =====================================================
-- VERIFICATION QUERIES (for testing)
-- =====================================================
-- Run these after migration to verify audit columns exist:
-- SHOW COLUMNS FROM users LIKE '%_by';
-- SHOW COLUMNS FROM products LIKE '%_by';
-- SHOW COLUMNS FROM orders LIKE '%_by';
-- SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
--   WHERE TABLE_SCHEMA = DATABASE() 
--   AND COLUMN_NAME IN ('created_by', 'updated_by', 'created_at', 'updated_at')
--   ORDER BY TABLE_NAME, COLUMN_NAME;
