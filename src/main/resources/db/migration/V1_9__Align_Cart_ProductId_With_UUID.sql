-- Align cart product_id columns with product UUID identifiers

ALTER TABLE cart_items
    MODIFY COLUMN product_id VARCHAR(255) NOT NULL;

ALTER TABLE order_cart_items
    MODIFY COLUMN product_id VARCHAR(255) NOT NULL;
