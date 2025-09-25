-- Create suppliers table (공급업체)
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(50),
    email VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_seq BIGINT COMMENT '생성자 ID',
    updated_seq BIGINT COMMENT '수정자 ID',

    INDEX idx_suppliers_name (name),
    INDEX idx_suppliers_email (email),
    INDEX idx_suppliers_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;