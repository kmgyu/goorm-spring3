package io.goorm.board.mapper;

import io.goorm.board.dto.inventory.InventoryTransactionSearchDto;
import io.goorm.board.entity.InventoryTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 입고 이력 매퍼
 */
@Mapper
public interface StockReceivingMapper {

    /**
     * 입고 이력 등록
     */
    int insert(InventoryTransaction inventoryTransaction);

    /**
     * 입고 이력 조회 (상세)
     */
    InventoryTransaction findBySeq(@Param("receivingSeq") Long receivingSeq);

    /**
     * 입고 이력 목록 조회 (검색 조건 포함)
     */
    List<InventoryTransaction> findAll(@Param("searchDto") InventoryTransactionSearchDto searchDto);

    /**
     * 입고 이력 총 개수 (검색 조건 포함)
     */
    int countAll(@Param("searchDto") InventoryTransactionSearchDto searchDto);

    /**
     * 특정 상품의 입고 이력 조회
     */
    List<InventoryTransaction> findByProductSeq(@Param("productSeq") Long productSeq);

    /**
     * 특정 처리자의 입고 이력 조회
     */
    List<InventoryTransaction> findByProcessedBySeq(@Param("processedBySeq") Long processedBySeq);

    /**
     * 기간별 입고 이력 조회
     */
    List<InventoryTransaction> findByPeriod(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 엑셀 파일의 입고 이력 조회
     */
    List<InventoryTransaction> findByExcelFilename(@Param("excelFilename") String excelFilename);

    /**
     * 입고 이력 수정
     */
    int update(InventoryTransaction inventoryTransaction);

    /**
     * 입고 이력 삭제
     */
    int deleteBySeq(@Param("receivingSeq") Long receivingSeq);

    /**
     * 특정 엑셀 파일의 입고 이력 전체 삭제
     */
    int deleteByExcelFilename(@Param("excelFilename") String excelFilename);
}