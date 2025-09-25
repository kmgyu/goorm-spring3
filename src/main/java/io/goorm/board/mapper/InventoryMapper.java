package io.goorm.board.mapper;

import io.goorm.board.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 재고 관련 MyBatis Mapper 인터페이스
 */
@Mapper
public interface InventoryMapper {

    /**
     * 상품별 재고 조회
     */
    Optional<Inventory> findByProductSeq(@Param("productSeq") Long productSeq);

    /**
     * 상품별 재고 조회 (위치별)
     */
    Optional<Inventory> findByProductSeqAndLocation(@Param("productSeq") Long productSeq,
                                                  @Param("location") String location);

    /**
     * 여러 상품의 재고 일괄 조회
     */
    List<Inventory> findByProductSeqs(@Param("productSeqs") List<Long> productSeqs);

    /**
     * 위치별 전체 재고 조회
     */
    List<Inventory> findByLocation(@Param("location") String location);

    /**
     * 재고 부족 상품 조회
     */
    List<Inventory> findLowStockItems();

    /**
     * 품절 상품 조회
     */
    List<Inventory> findOutOfStockItems();

    /**
     * 발주 필요 상품 조회
     */
    List<Inventory> findReorderItems();

    /**
     * 재고 수량 업데이트
     */
    int updateStock(@Param("inventorySeq") Long inventorySeq,
                   @Param("currentStock") Integer currentStock,
                   @Param("reservedStock") Integer reservedStock);

    /**
     * 재고 예약 (주문 시)
     */
    int reserveStock(@Param("productSeq") Long productSeq,
                    @Param("quantity") Integer quantity);

    /**
     * 재고 예약 해제 (주문 취소 시)
     */
    int releaseReservedStock(@Param("productSeq") Long productSeq,
                            @Param("quantity") Integer quantity);

    /**
     * 재고 소모 처리 (배송 완료 시)
     */
    int consumeStock(@Param("productSeq") Long productSeq,
                    @Param("quantity") Integer quantity);

    /**
     * 재고 차감 (주문 승인 시)
     */
    int decreaseStock(@Param("productSeq") Long productSeq,
                     @Param("quantity") Integer quantity);

    /**
     * 재고 증가 (입고 처리 시)
     */
    int increaseStock(@Param("productSeq") Long productSeq,
                     @Param("quantity") Integer quantity);

    /**
     * 신규 재고 생성
     */
    int insert(Inventory inventory);
}