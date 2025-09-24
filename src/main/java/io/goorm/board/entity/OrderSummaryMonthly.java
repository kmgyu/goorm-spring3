package io.goorm.board.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 월별 발주 집계 엔티티
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryMonthly {

    private Long summarySeq;
    private Long companySeq;
    private String summaryYear;
    private String summaryMonth;
    private Integer orderCount;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 조인 필드
    private String companyName;
}