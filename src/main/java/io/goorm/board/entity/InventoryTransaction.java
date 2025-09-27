package io.goorm.board.entity;

import io.goorm.board.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고 거래 이력 엔티티 (입고/출고 통합)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {
    private Long transactionSeq;        // 거래이력 시퀀스 (기존: receivingSeq)
    private TransactionType transactionType; // 거래 유형 (RECEIVING/ORDER_CONSUMED)
    private Long productSeq;            // 상품 시퀀스
    private Long categorySeq;           // 카테고리 시퀀스
    private Integer quantity;           // 수량 (입고: +, 출고: -)
    private BigDecimal unitPrice;       // 단가
    private BigDecimal totalAmount;     // 총 금액
    private String note;                // 비고
    private Long processedBySeq;        // 처리자 시퀀스

    // 입고 관련 필드 (RECEIVING 타입일 때만 사용)
    private String excelFilename;       // 엑셀 파일명
    private String excelFilepath;       // 엑셀 파일 전체 경로
    private Integer excelRowNum;        // 엑셀 내 행 번호

    // 출고 관련 필드 (ORDER_CONSUMED 타입일 때만 사용)
    private Long orderSeq;              // 주문 시퀀스

    private LocalDateTime processedAt;  // 처리 일시
    private LocalDateTime createdAt;    // 생성 일시
    private LocalDateTime updatedAt;    // 수정 일시

    // 조인을 위한 추가 필드 (조회 시에만 사용)
    private String productName;         // 상품명 (products 테이블)
    private String productCode;         // 상품코드 (products 테이블)
    private String categoryName;        // 카테고리명 (categories 테이블)
    private String processedByName;     // 처리자명 (users 테이블)
    private String processedByEmail;    // 처리자 이메일 (users 테이블)

    // 주문 관련 조인 필드 (ORDER_CONSUMED 타입일 때만 사용)
    private String orderNumber;         // 주문번호 (orders 테이블)
}