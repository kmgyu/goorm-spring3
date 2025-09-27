package io.goorm.board.mapper;

import io.goorm.board.dto.inventory.InventoryTransactionSearchDto;
import io.goorm.board.entity.InventoryTransaction;
import io.goorm.board.enums.TransactionType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 재고 거래 이력 매퍼 (입고/출고 통합)
 */
@Mapper
public interface InventoryTransactionMapper {

    /**
     * 거래 이력 등록
     */
    int insert(InventoryTransaction inventoryTransaction);

    /**
     * 거래 이력 조회 (상세)
     */
    InventoryTransaction findBySeq(@Param("transactionSeq") Long transactionSeq);

    /**
     * 거래 이력 목록 조회 (검색 조건 포함)
     */
    List<InventoryTransaction> findAll(@Param("searchDto") InventoryTransactionSearchDto searchDto);

    /**
     * 거래 이력 총 개수 (검색 조건 포함)
     */
    int countAll(@Param("searchDto") InventoryTransactionSearchDto searchDto);

    /**
     * 특정 거래 유형의 이력 조회
     */
    List<InventoryTransaction> findByTransactionType(@Param("transactionType") TransactionType transactionType);

    /**
     * 특정 상품의 거래 이력 조회
     */
    List<InventoryTransaction> findByProductSeq(@Param("productSeq") Long productSeq);

    /**
     * 특정 처리자의 거래 이력 조회
     */
    List<InventoryTransaction> findByProcessedBySeq(@Param("processedBySeq") Long processedBySeq);

    /**
     * 기간별 거래 이력 조회
     */
    List<InventoryTransaction> findByPeriod(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 주문의 출고 이력 조회 (ORDER_CONSUMED 타입)
     */
    List<InventoryTransaction> findByOrderSeq(@Param("orderSeq") Long orderSeq);

    /**
     * 특정 엑셀 파일의 입고 이력 조회 (RECEIVING 타입)
     */
    List<InventoryTransaction> findByExcelFilename(@Param("excelFilename") String excelFilename);

    /**
     * 거래 이력 수정
     */
    int update(InventoryTransaction inventoryTransaction);

    /**
     * 거래 이력 삭제
     */
    int deleteBySeq(@Param("transactionSeq") Long transactionSeq);
}