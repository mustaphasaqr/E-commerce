-- Align cart product_id columns with product UUID identifiers

ALTER TABLE cart_items
    MODIFY COLUMN product_id VARCHAR(255) NOT NULL;

ALTER TABLE order_cart_items
    MODIFY COLUMN product_id VARCHAR(255) NOT NULL;

ALTER TABLE carts
    MODIFY COLUMN user_id VARCHAR(255) NULL;

ALTER TABLE product_reviews
    MODIFY COLUMN product_id VARCHAR(255) NOT NULL,
    MODIFY COLUMN customer_id VARCHAR(255) NOT NULL;
