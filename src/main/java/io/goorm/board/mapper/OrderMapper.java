package io.goorm.board.mapper;

import io.goorm.board.dto.order.OrderSearchDto;
import io.goorm.board.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 발주 관련 MyBatis Mapper 인터페이스
 */
@Mapper
public interface OrderMapper {

    /**
     * 발주 등록
     */
    int insert(Order order);

    /**
     * 배송완료 + 인보이스 발행 통합 처리
     */
    int updateDeliveryComplete(Order order);

    /**
     * 입금완료 처리
     */
    int updatePaymentComplete(Order order);


    /**
     * 발주 조회 (ID)
     */
    Optional<Order> findById(Long orderSeq);

    /**
     * 발주 조회 (발주번호)
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 발주 목록 조회 (검색 조건)
     */
    List<Order> findAll(OrderSearchDto searchDto);

    /**
     * 발주 총 개수 (검색 조건)
     */
    long countAll(OrderSearchDto searchDto);

    /**
     * 회사별 발주 목록 조회
     */
    List<Order> findByCompanySeq(@Param("companySeq") Long companySeq, @Param("searchDto") OrderSearchDto searchDto);

    /**
     * 회사별 발주 총 개수
     */
    long countByCompanySeq(@Param("companySeq") Long companySeq, @Param("searchDto") OrderSearchDto searchDto);

    /**
     * 미처리 발주 목록 (관리자 대시보드용)
     */
    List<Order> findPendingOrders();

    /**
     * 발주번호 생성을 위한 당일 최대 시퀀스 조회
     */
    Integer findMaxDailySequence(String datePrefix);

}