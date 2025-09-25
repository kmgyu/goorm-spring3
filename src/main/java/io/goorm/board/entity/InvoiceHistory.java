package io.goorm.board.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 인보이스 출력 이력 엔티티
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceHistory {

    private Long printSeq;              // 출력 시퀀스
    private Long orderSeq;              // 주문 시퀀스
    private String invoiceId;           // 인보이스 ID (INV-20251224-001)
    private LocalDateTime printedAt;    // 출력일시
    private Long printedBySeq;          // 출력자 시퀀스
    private String printedBy;           // 출력자명
    private Integer printCount;         // 출력 횟수
    private LocalDateTime createdAt;    // 생성일시

    // 조인 필드 (필요시)
    private String orderNumber;         // 주문번호
    private String companyName;         // 회사명
}