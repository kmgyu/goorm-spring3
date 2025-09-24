-- 할인율 이력 및 월별 발주 집계 테이블 생성

-- 회사별 할인율 이력 테이블
CREATE TABLE IF NOT EXISTS company_discount_history (
    history_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_seq BIGINT NOT NULL COMMENT '회사 번호',
    apply_year VARCHAR(4) NOT NULL COMMENT '적용 연도',
    previous_year_amount DECIMAL(15,2) DEFAULT 0 COMMENT '전년도 발주 총액',
    discount_rate DECIMAL(5,2) NOT NULL COMMENT '할인율 (%)',
    effective_from DATE NOT NULL COMMENT '적용 시작일',
    effective_to DATE COMMENT '적용 종료일',
    reason VARCHAR(200) COMMENT '변경 사유',
    created_by VARCHAR(50) COMMENT '등록자',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (company_seq) REFERENCES companies(company_seq),
    INDEX idx_company_effective (company_seq, effective_from, effective_to)
) COMMENT '회사별 할인율 이력 테이블';

-- 월별 발주 집계 테이블 (전년도 12개월 데이터)
CREATE TABLE IF NOT EXISTS order_summary_monthly (
    summary_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_seq BIGINT NOT NULL COMMENT '회사 번호',
    summary_year VARCHAR(4) NOT NULL COMMENT '연도',
    summary_month VARCHAR(2) NOT NULL COMMENT '월',
    order_count INT DEFAULT 0 COMMENT '발주 건수',
    total_amount DECIMAL(15,2) DEFAULT 0 COMMENT '총 발주 금액',
    discount_amount DECIMAL(15,2) DEFAULT 0 COMMENT '할인 금액',
    final_amount DECIMAL(15,2) DEFAULT 0 COMMENT '최종 금액',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (company_seq) REFERENCES companies(company_seq),
    UNIQUE KEY uk_company_year_month (company_seq, summary_year, summary_month),
    INDEX idx_year_month (summary_year, summary_month)
) COMMENT '월별 발주 집계 테이블';