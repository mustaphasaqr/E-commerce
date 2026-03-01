-- Add payment tracking columns to orders table
-- Accept (Paymob) payment integration requires storing checkout_id and transaction_id

-- Add checkout_id column (stores Accept payment key / checkout session ID)
ALTER TABLE orders ADD COLUMN checkout_id VARCHAR(255);

-- Add transaction_id column (stores Accept transaction ID after successful payment)
ALTER TABLE orders ADD COLUMN transaction_id VARCHAR(255);

-- Create index for fast lookup by checkout_id (used during payment verification)
CREATE INDEX idx_orders_checkout_id ON orders(checkout_id);

-- Note: transaction_id doesn't need an index as we look up orders by order_id, not transaction_id
