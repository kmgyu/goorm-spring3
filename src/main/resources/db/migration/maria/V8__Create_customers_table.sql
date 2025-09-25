-- Create customers table (고객)
CREATE TABLE IF NOT EXISTS customers (
    customer_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    company VARCHAR(100),
    tax_number VARCHAR(50),
    billing_address TEXT,
    shipping_address TEXT,
    customer_type ENUM('INDIVIDUAL', 'BUSINESS') DEFAULT 'INDIVIDUAL',
    credit_limit DECIMAL(12,2) DEFAULT 0.00,
    payment_terms INT DEFAULT 30,
    discount_rate DECIMAL(5,2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_seq BIGINT COMMENT '생성자 ID',
    updated_seq BIGINT COMMENT '수정자 ID',

    INDEX idx_customers_code (customer_code),
    INDEX idx_customers_name (name),
    INDEX idx_customers_email (email),
    INDEX idx_customers_phone (phone),
    INDEX idx_customers_type (customer_type),
    INDEX idx_customers_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;