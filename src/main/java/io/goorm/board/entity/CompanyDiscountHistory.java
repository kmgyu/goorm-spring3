package io.goorm.board.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 회사별 할인율 이력 엔티티
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDiscountHistory {

    private Long historySeq;
    private Long companySeq;
    private String applyYear;
    private BigDecimal previousYearAmount;
    private BigDecimal discountRate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String reason;
    private String createdBy;
    private LocalDateTime createdAt;

    // 조인 필드
    private String companyName;
}