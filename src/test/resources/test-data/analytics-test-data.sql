-- Clear existing data
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM products;
DELETE FROM users;

-- Insert test user
INSERT INTO users (id, username, email, hashed_password, role, status, email_verified, deleted, terms_accepted, marketing_consent_given, created_at, updated_at, created_by, updated_by)
VALUES ('1', 'test_user', 'test@example.com', 'hashed_password', 'CUSTOMER', 'ACTIVE', true, false, true, false, TIMESTAMP '2026-01-01 00:00:00', TIMESTAMP '2026-01-01 00:00:00', 'SYSTEM', 'SYSTEM');

-- Insert test products
INSERT INTO products (id, sku, name, description, price, cost_of_goods, currency, total_stock, available_stock, reserved_stock, active, visible, available_for_purchase, discontinued, created_at, updated_at, created_by, updated_by)
VALUES ('PROD-001', 'SKU-LAPTOP', 'Laptop', 'Test Laptop', 1000.00, 600.00, 'USD', 10, 10, 0, true, true, true, false, TIMESTAMP '2026-01-01 00:00:00', TIMESTAMP '2026-01-01 00:00:00', 'SYSTEM', 'SYSTEM');

INSERT INTO products (id, sku, name, description, price, cost_of_goods, currency, total_stock, available_stock, reserved_stock, active, visible, available_for_purchase, discontinued, created_at, updated_at, created_by, updated_by)
VALUES ('PROD-002', 'SKU-MOUSE', 'Mouse', 'Test Mouse', 25.00, 10.00, 'USD', 50, 50, 0, true, true, true, false, TIMESTAMP '2026-01-01 00:00:00', TIMESTAMP '2026-01-01 00:00:00', 'SYSTEM', 'SYSTEM');

-- Insert test order with custom timestamp (Jan 11, 2026 at 10:00 AM)
INSERT INTO orders (id, customer_id, status, total_amount, created_at, updated_at, created_by, updated_by)
VALUES ('ORD-001', '1', 'DELIVERED', 0.00, TIMESTAMP '2026-01-11 10:00:00', TIMESTAMP '2026-01-11 10:00:00', 'TEST', 'TEST');

-- Insert order items (id is auto-generated)
INSERT INTO order_items (order_id, product_id, product_name, quantity, price)
VALUES ('ORD-001', 'PROD-001', 'Laptop', 5, 1000.00);

INSERT INTO order_items (order_id, product_id, product_name, quantity, price)
VALUES ('ORD-001', 'PROD-002', 'Mouse', 20, 25.00);
