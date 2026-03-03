-- V1_6__Add_Analytics_Fields_To_Orders.sql
-- Add fields for Geographic, Shipping, and Marketing analytics

-- Geographic Analytics Fields
ALTER TABLE orders 
ADD COLUMN shipping_city VARCHAR(100) COMMENT 'City for geographic sales analytics',
ADD COLUMN shipping_state VARCHAR(100) COMMENT 'State/Province for regional analytics',
ADD COLUMN shipping_country VARCHAR(100) COMMENT 'Country for international analytics',
ADD COLUMN shipping_zip_code VARCHAR(20) COMMENT 'Postal code for location analytics';

-- Shipping Performance Analytics
ALTER TABLE orders 
ADD COLUMN shipped_at TIMESTAMP NULL COMMENT 'When order was shipped for performance tracking';

-- Marketing Attribution Analytics
ALTER TABLE orders 
ADD COLUMN utm_source VARCHAR(100) COMMENT 'Marketing source (google, facebook, email, direct)',
ADD COLUMN utm_campaign VARCHAR(100) COMMENT 'Marketing campaign identifier',
ADD COLUMN referrer VARCHAR(500) COMMENT 'HTTP referrer for attribution tracking';

-- Add indexes for analytics queries
CREATE INDEX idx_orders_shipping_location ON orders(shipping_city, shipping_state, shipping_country);
CREATE INDEX idx_orders_shipped_at ON orders(shipped_at);
CREATE INDEX idx_orders_utm_source ON orders(utm_source);
CREATE INDEX idx_orders_utm_campaign ON orders(utm_campaign);
CREATE INDEX idx_orders_created_analytics ON orders(created_at, status);
