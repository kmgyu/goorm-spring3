-- Create products table (상품)
CREATE TABLE IF NOT EXISTS products (
    product_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category_seq BIGINT,
    supplier_seq BIGINT,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    unit_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    unit VARCHAR(20) DEFAULT 'EA',
    sku VARCHAR(100),
    barcode VARCHAR(100),
    weight DECIMAL(8,3) DEFAULT 0.000,
    dimensions VARCHAR(100),
    image_url VARCHAR(500),
    status ENUM('ACTIVE', 'INACTIVE', 'DISCONTINUED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_seq BIGINT COMMENT '생성자 ID',
    updated_seq BIGINT COMMENT '수정자 ID',

    INDEX idx_products_code (code),
    INDEX idx_products_name (name),
    INDEX idx_products_category_seq (category_seq),
    INDEX idx_products_supplier_seq (supplier_seq),
    INDEX idx_products_sku (sku),
    INDEX idx_products_barcode (barcode),
    INDEX idx_products_status (status),

    CONSTRAINT fk_products_category_seq
        FOREIGN KEY (category_seq)
        REFERENCES categories(category_seq)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT fk_products_supplier_seq
        FOREIGN KEY (supplier_seq)
        REFERENCES suppliers(supplier_seq)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;