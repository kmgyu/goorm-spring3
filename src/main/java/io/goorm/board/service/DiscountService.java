package io.goorm.board.service;

import io.goorm.board.entity.CompanyDiscountHistory;
import io.goorm.board.mapper.CompanyDiscountHistoryMapper;
import io.goorm.board.mapper.OrderSummaryMonthlyMapper;
import io.goorm.board.service.discount.DiscountCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscountService {

    private final CompanyDiscountHistoryMapper discountHistoryMapper;
    private final OrderSummaryMonthlyMapper summaryMapper;
    private final DiscountCalculator discountCalculator;

    /**
     * 회사별 현재 적용 할인율 계산
     */
    public BigDecimal calculateDiscountRate(Long companySeq) {
        List<CompanyDiscountHistory> histories = discountHistoryMapper.findByCompanySeq(companySeq);
        return discountCalculator.calculateCurrentDiscountRate(histories, LocalDate.now());
    }

    /**
     * 주문 금액에 할인 적용
     */
    public BigDecimal applyDiscount(BigDecimal orderAmount, Long companySeq) {
        BigDecimal discountRate = calculateDiscountRate(companySeq);
        return discountCalculator.applyDiscount(orderAmount, discountRate);
    }

    /**
     * 최종 결제 금액 계산
     */
    public BigDecimal calculateFinalAmount(BigDecimal orderAmount, Long companySeq) {
        BigDecimal discountAmount = applyDiscount(orderAmount, companySeq);
        return discountCalculator.calculateFinalAmount(orderAmount, discountAmount);
    }

    /**
     * 전년도 구매액 조회
     */
    public BigDecimal getPreviousYearAmount(Long companySeq, String year) {
        return summaryMapper.findPreviousYearTotalAmount(companySeq, year);
    }

    /**
     * 전년도 구매액 기준 할인 등급 평가
     */
    public DiscountCalculator.DiscountGrade evaluateDiscountGrade(Long companySeq, String year) {
        BigDecimal previousYearAmount = getPreviousYearAmount(companySeq, year);
        return discountCalculator.evaluateDiscountGrade(previousYearAmount);
    }

    /**
     * 할인 등급별 권장 할인율 조회
     */
    public BigDecimal getRecommendedDiscountRate(Long companySeq, String year) {
        DiscountCalculator.DiscountGrade grade = evaluateDiscountGrade(companySeq, year);
        return discountCalculator.getRecommendedDiscountRate(grade);
    }
}