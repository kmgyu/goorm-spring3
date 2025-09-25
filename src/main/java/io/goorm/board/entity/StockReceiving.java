package io.goorm.board.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 입고 이력 엔티티
 */
@Data
public class StockReceiving {
    private Long receivingSeq;          // 입고이력 시퀀스
    private Long productSeq;            // 상품 시퀀스
    private Long categorySeq;           // 카테고리 시퀀스
    private Integer quantity;           // 입고 수량
    private BigDecimal unitPrice;       // 입고 단가
    private BigDecimal totalAmount;     // 총 입고 금액
    private String note;                // 비고
    private Long processedBySeq;        // 처리자 시퀀스
    private String excelFilename;       // 엑셀 파일명
    private String excelFilepath;       // 엑셀 파일 전체 경로
    private Integer excelRowNum;        // 엑셀 내 행 번호
    private LocalDateTime processedAt;  // 처리 일시
    private LocalDateTime createdAt;    // 생성 일시
    private LocalDateTime updatedAt;    // 수정 일시

    // 조인을 위한 추가 필드 (조회 시에만 사용)
    private String productName;         // 상품명 (products 테이블)
    private String productCode;         // 상품코드 (products 테이블)
    private String categoryName;        // 카테고리명 (categories 테이블)
    private String processedByName;     // 처리자명 (users 테이블)
    private String processedByEmail;    // 처리자 이메일 (users 테이블)
}