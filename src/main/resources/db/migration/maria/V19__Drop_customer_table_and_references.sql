-- Drop customer_seq column and customers table
-- Remove FK constraint first
ALTER TABLE orders DROP FOREIGN KEY fk_orders_customer_seq;

-- Drop customer_seq column from orders table
ALTER TABLE orders DROP COLUMN customer_seq;

-- Drop customers table completely
DROP TABLE IF EXISTS customers;