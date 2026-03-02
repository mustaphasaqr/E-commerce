-- V1_5__Create_Abandoned_Cart_Tracking.sql
-- Schema for tracking abandoned cart recovery

CREATE TABLE abandoned_cart_reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    opened BOOLEAN DEFAULT FALSE,
    clicked BOOLEAN DEFAULT FALSE,
    recovered BOOLEAN DEFAULT FALSE,
    recovery_amount DECIMAL(10, 2),
    INDEX idx_customer_id (customer_id),
    INDEX idx_sent_at (sent_at)
);

-- Track customer activity events for recommendation engine
CREATE TABLE customer_activity_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL, -- PRODUCT_VIEW, ADD_TO_CART, REMOVE_FROM_CART, PURCHASE
    product_id BIGINT,
    event_data JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer_id (customer_id),
    INDEX idx_event_type (event_type),
    INDEX idx_product_id (product_id),
    INDEX idx_created_at (created_at)
);
