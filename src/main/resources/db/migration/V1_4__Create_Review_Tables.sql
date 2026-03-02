-- V1_4__Create_Review_Tables.sql
-- Product Reviews & Ratings System Schema

CREATE TABLE product_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    order_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(200),
    review_text TEXT,
    is_verified_purchase BOOLEAN NOT NULL DEFAULT TRUE,
    helpful_count INT NOT NULL DEFAULT 0,
    not_helpful_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, FLAGGED
    admin_response TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE KEY unique_review_per_order_product (order_id, product_id, customer_id),
    INDEX idx_product_id (product_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_rating (rating),
    INDEX idx_created_at (created_at)
);

-- Review helpfulness votes (to prevent duplicate votes)
CREATE TABLE review_votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    vote_type VARCHAR(20) NOT NULL, -- HELPFUL, NOT_HELPFUL
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_vote_per_customer (review_id, customer_id),
    INDEX idx_review_id (review_id),
    INDEX idx_customer_id (customer_id),
    FOREIGN KEY (review_id) REFERENCES product_reviews(id) ON DELETE CASCADE
);

-- Review flags/reports
CREATE TABLE review_flags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_review_id (review_id),
    INDEX idx_customer_id (customer_id),
    FOREIGN KEY (review_id) REFERENCES product_reviews(id) ON DELETE CASCADE
);
