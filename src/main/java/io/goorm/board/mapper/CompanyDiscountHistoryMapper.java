package io.goorm.board.mapper;

import io.goorm.board.entity.CompanyDiscountHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 회사별 할인율 이력 MyBatis Mapper 인터페이스
 */
@Mapper
public interface CompanyDiscountHistoryMapper {

    /**
     * 할인율 이력 등록
     */
    int insert(CompanyDiscountHistory history);

    /**
     * 회사별 현재 할인율 조회
     */
    Optional<BigDecimal> findCurrentDiscountRate(@Param("companySeq") Long companySeq, @Param("currentDate") LocalDate currentDate);

    /**
     * 회사별 할인율 이력 조회
     */
    List<CompanyDiscountHistory> findByCompanySeq(Long companySeq);

    /**
     * 전체 할인율 이력 조회
     */
    List<CompanyDiscountHistory> findAll();
}