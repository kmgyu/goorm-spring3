package io.goorm.board.mapper;

import io.goorm.board.entity.InvoiceHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 인보이스 출력 이력 MyBatis Mapper 인터페이스
 */
@Mapper
public interface InvoiceHistoryMapper {

    /**
     * 인보이스 출력 이력 등록
     */
    int insert(InvoiceHistory invoiceHistory);

    /**
     * 주문별 인보이스 출력 이력 조회
     */
    List<InvoiceHistory> findByOrderSeq(@Param("orderSeq") Long orderSeq);

    /**
     * 인보이스 ID로 조회
     */
    List<InvoiceHistory> findByInvoiceId(@Param("invoiceId") String invoiceId);

    /**
     * 사용자별 인보이스 출력 이력 조회
     */
    List<InvoiceHistory> findByPrintedBySeq(@Param("printedBySeq") Long printedBySeq);
}