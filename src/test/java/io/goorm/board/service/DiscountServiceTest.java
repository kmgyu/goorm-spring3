package io.goorm.board.service;

import io.goorm.board.mapper.OrderMapper;
import io.goorm.board.mapper.OrderSummaryMonthlyMapper;
import io.goorm.board.service.discount.DiscountCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("할인 서비스 테스트")
class DiscountServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderSummaryMonthlyMapper summaryMapper;

    @Mock
    private DiscountCalculator discountCalculator;

    @InjectMocks
    private DiscountService discountService;

    @DisplayName("회사별 현재 할인율 계산 - 전년도 구매액 기준")
    @Test
    void calculateDiscountRate_ValidCase() {
        // Given
        Long companySeq = 1L;
        BigDecimal previousYearAmount = new BigDecimal("100000000");
        DiscountCalculator.DiscountGrade grade = DiscountCalculator.DiscountGrade.GOLD;
        BigDecimal expectedRate = new BigDecimal("5.0");

        given(summaryMapper.findPreviousYearTotalAmount(eq(companySeq), anyString())).willReturn(previousYearAmount);
        given(discountCalculator.evaluateDiscountGrade(previousYearAmount)).willReturn(grade);
        given(discountCalculator.getRecommendedDiscountRate(grade)).willReturn(expectedRate);

        // When
        BigDecimal result = discountService.calculateDiscountRate(companySeq);

        // Then
        assertThat(result).isEqualTo(expectedRate);
        verify(summaryMapper).findPreviousYearTotalAmount(eq(companySeq), anyString());
        verify(discountCalculator).evaluateDiscountGrade(previousYearAmount);
        verify(discountCalculator).getRecommendedDiscountRate(grade);
    }

    @DisplayName("회사별 현재 할인율 계산 - 구매 이력 없음")
    @Test
    void calculateDiscountRate_NoHistory() {
        // Given
        Long companySeq = 1L;
        BigDecimal zeroPreviousAmount = BigDecimal.ZERO;
        DiscountCalculator.DiscountGrade grade = DiscountCalculator.DiscountGrade.NONE;

        given(summaryMapper.findPreviousYearTotalAmount(eq(companySeq), anyString())).willReturn(zeroPreviousAmount);
        given(discountCalculator.evaluateDiscountGrade(zeroPreviousAmount)).willReturn(grade);
        given(discountCalculator.getRecommendedDiscountRate(grade)).willReturn(BigDecimal.ZERO);

        // When
        BigDecimal result = discountService.calculateDiscountRate(companySeq);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("주문 금액에 할인 적용")
    @Test
    void applyDiscount_ValidCase() {
        // Given
        Long companySeq = 1L;
        BigDecimal orderAmount = new BigDecimal("100000");
        BigDecimal discountRate = new BigDecimal("5.0");
        BigDecimal expectedDiscount = new BigDecimal("5000");

        given(summaryMapper.findPreviousYearTotalAmount(eq(companySeq), anyString())).willReturn(new BigDecimal("100000000"));
        given(discountCalculator.evaluateDiscountGrade(any())).willReturn(DiscountCalculator.DiscountGrade.GOLD);
        given(discountCalculator.getRecommendedDiscountRate(any())).willReturn(discountRate);
        given(discountCalculator.applyDiscount(orderAmount, discountRate)).willReturn(expectedDiscount);

        // When
        BigDecimal result = discountService.applyDiscount(orderAmount, companySeq);

        // Then
        assertThat(result).isEqualTo(expectedDiscount);
        verify(discountCalculator).applyDiscount(orderAmount, discountRate);
    }

    @DisplayName("전년도 구매액 조회")
    @Test
    void getPreviousYearAmount_ValidCase() {
        // Given
        Long companySeq = 1L;
        String year = "2024";
        BigDecimal expectedAmount = new BigDecimal("150000000");

        given(summaryMapper.findPreviousYearTotalAmount(companySeq, year)).willReturn(expectedAmount);

        // When
        BigDecimal result = discountService.getPreviousYearAmount(companySeq, year);

        // Then
        assertThat(result).isEqualTo(expectedAmount);
        verify(summaryMapper).findPreviousYearTotalAmount(companySeq, year);
    }
}