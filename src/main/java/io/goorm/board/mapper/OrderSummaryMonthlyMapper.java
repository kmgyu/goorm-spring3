package io.goorm.board.mapper;

import io.goorm.board.entity.OrderSummaryMonthly;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 월별 발주 집계 MyBatis Mapper 인터페이스
 */
@Mapper
public interface OrderSummaryMonthlyMapper {

    /**
     * 월별 집계 등록
     */
    int insert(OrderSummaryMonthly summary);

    /**
     * 월별 집계 수정
     */
    int update(OrderSummaryMonthly summary);

    /**
     * 회사별 전년도 총 발주액 조회
     */
    BigDecimal findPreviousYearTotalAmount(@Param("companySeq") Long companySeq, @Param("year") String year);

    /**
     * 회사별 월별 집계 조회
     */
    List<OrderSummaryMonthly> findByCompanySeq(@Param("companySeq") Long companySeq, @Param("year") String year);

    /**
     * 전체 월별 집계 조회
     */
    List<OrderSummaryMonthly> findAll(@Param("year") String year);
}