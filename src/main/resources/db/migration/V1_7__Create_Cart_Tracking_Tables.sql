-- V1_7__Create_Cart_Tracking_Tables.sql
-- Create tables for cart abandonment analytics

-- Main carts table for tracking shopping carts
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT 'Customer ID if logged in',
    session_id VARCHAR(255) NOT NULL COMMENT 'Session identifier for anonymous users',
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'Total cart value',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Cart status: ACTIVE, CONVERTED, ABANDONED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When cart was created',
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last cart modification',
    converted_order_id BIGINT COMMENT 'Order ID if cart was converted to order',
    version INT NOT NULL DEFAULT 0 COMMENT 'Optimistic locking version',
    
    INDEX idx_carts_user_id (user_id),
    INDEX idx_carts_session_id (session_id),
    INDEX idx_carts_status (status),
    INDEX idx_carts_created_at (created_at),
    INDEX idx_carts_last_updated (last_updated_at),
    INDEX idx_carts_converted_order (converted_order_id),
    
    CONSTRAINT chk_cart_status CHECK (status IN ('ACTIVE', 'CONVERTED', 'ABANDONED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Shopping carts for abandonment analytics';

-- Cart items table for cart line items
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL COMMENT 'Foreign key to carts table',
    product_id BIGINT NOT NULL COMMENT 'Product being added to cart',
    product_name VARCHAR(255) NOT NULL COMMENT 'Product name snapshot',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Quantity of product',
    price DECIMAL(10, 2) NOT NULL COMMENT 'Product price snapshot',
    
    INDEX idx_cart_items_cart_id (cart_id),
    INDEX idx_cart_items_product_id (product_id),
    
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_cart_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_cart_item_price CHECK (price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Line items in shopping carts';

-- Create trigger to mark old carts as abandoned (optional - can be done via scheduled job)
-- This will run daily to mark carts older than 24 hours as ABANDONED
DELIMITER $$

CREATE EVENT IF NOT EXISTS mark_abandoned_carts
ON SCHEDULE EVERY 1 HOUR
DO
BEGIN
    UPDATE carts 
    SET status = 'ABANDONED'
    WHERE status = 'ACTIVE' 
    AND last_updated_at < DATE_SUB(NOW(), INTERVAL 24 HOUR);
END$$

DELIMITER ;
