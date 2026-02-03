-- Add check constraint for product reservations quantity
-- This ensures reservation quantities are always positive
-- Applied to production MySQL database (H2 test database handles via @Column nullable=false)

ALTER TABLE product_reservations 
ADD CONSTRAINT chk_reservation_qty_positive CHECK (quantity > 0);
