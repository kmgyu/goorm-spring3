package io.goorm.board.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 발주 상품 엔티티
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    private Long orderItemSeq;
    private Long orderSeq;
    private Long productSeq;
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDateTime createdAt;

    // 조인 필드
    private String productName;
    private String productCode;
    private String categoryName;

    /**
     * 라인 총액 계산 (단가 × 수량)
     */
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}