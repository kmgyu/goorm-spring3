package io.goorm.board.dto.stockreceiving;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 입고 이력 검색 DTO
 */
@Data
public class StockReceivingSearchDto {

    // 기본 검색 조건
    private String keyword;                 // 검색 키워드 (상품명, 상품코드, 처리자명 등)
    private Long productSeq;               // 상품 시퀀스
    private Long categorySeq;              // 카테고리 시퀀스
    private Long processedBySeq;           // 처리자 시퀀스
    private String excelFilename;          // 엑셀 파일명

    // 날짜 검색
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;           // 시작일

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;             // 종료일

    // 페이징
    private int page = 0;                  // 페이지 번호 (0부터 시작)
    private int size = 20;                 // 페이지 크기
    private int offset;                    // 오프셋 (계산됨)

    // 정렬
    private String sortBy = "processedAt"; // 정렬 기준
    private String sortDir = "DESC";       // 정렬 방향 (ASC, DESC)

    /**
     * 오프셋 계산
     */
    public int getOffset() {
        return page * size;
    }

    /**
     * 시작일시 반환 (LocalDateTime 변환)
     */
    public LocalDateTime getStartDateTime() {
        return startDate != null ? startDate.atStartOfDay() : null;
    }

    /**
     * 종료일시 반환 (LocalDateTime 변환 - 해당일의 마지막 시간)
     */
    public LocalDateTime getEndDateTime() {
        return endDate != null ? endDate.atTime(LocalTime.MAX) : null;
    }

    /**
     * 검색 조건 존재 여부
     */
    public boolean hasSearchCondition() {
        return (keyword != null && !keyword.trim().isEmpty()) ||
               productSeq != null ||
               categorySeq != null ||
               processedBySeq != null ||
               (excelFilename != null && !excelFilename.trim().isEmpty()) ||
               startDate != null ||
               endDate != null;
    }
}