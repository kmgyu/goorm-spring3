-- 입고 이력 테이블 생성
CREATE TABLE stock_receiving (
    receiving_seq BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '입고이력 시퀀스',
    product_seq BIGINT NOT NULL COMMENT '상품 시퀀스',
    category_seq BIGINT NOT NULL COMMENT '카테고리 시퀀스',
    quantity INT NOT NULL COMMENT '입고 수량',
    unit_price DECIMAL(15,2) NOT NULL COMMENT '입고 단가',
    total_amount DECIMAL(15,2) NOT NULL COMMENT '총 입고 금액 (quantity * unit_price)',
    note TEXT COMMENT '비고',
    processed_by_seq BIGINT NOT NULL COMMENT '처리자 시퀀스',
    excel_filename VARCHAR(255) NOT NULL COMMENT '업로드된 엑셀 파일명',
    excel_filepath TEXT NOT NULL COMMENT '엑셀 파일 전체 경로',
    excel_row_num INT NOT NULL COMMENT '엑셀 내 행 번호',
    processed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '처리 일시',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    -- 외래키 제약조건
    CONSTRAINT fk_stock_receiving_product FOREIGN KEY (product_seq) REFERENCES products(product_seq),
    CONSTRAINT fk_stock_receiving_category FOREIGN KEY (category_seq) REFERENCES categories(category_seq),
    CONSTRAINT fk_stock_receiving_processor FOREIGN KEY (processed_by_seq) REFERENCES users(user_seq),

    -- 인덱스
    INDEX idx_stock_receiving_product (product_seq),
    INDEX idx_stock_receiving_category (category_seq),
    INDEX idx_stock_receiving_processor (processed_by_seq),
    INDEX idx_stock_receiving_processed_at (processed_at),
    INDEX idx_stock_receiving_excel_filename (excel_filename),

    -- 체크 제약조건
    CONSTRAINT chk_stock_receiving_quantity CHECK (quantity > 0),
    CONSTRAINT chk_stock_receiving_unit_price CHECK (unit_price >= 0)
)
COMMENT '입고 이력 테이블';