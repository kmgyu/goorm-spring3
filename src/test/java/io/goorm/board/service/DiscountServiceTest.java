package io.goorm.board.service;

import io.goorm.board.entity.CompanyDiscountHistory;
import io.goorm.board.mapper.CompanyDiscountHistoryMapper;
import io.goorm.board.mapper.OrderSummaryMonthlyMapper;
import io.goorm.board.service.discount.DiscountCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("할인 서비스 통합 테스트")
class DiscountServiceTest {

    @Mock
    private CompanyDiscountHistoryMapper discountHistoryMapper;

    @Mock
    private OrderSummaryMonthlyMapper summaryMapper;

    @Mock
    private DiscountCalculator discountCalculator;

    @InjectMocks
    private DiscountService discountService;

    @DisplayName("회사별 현재 할인율 계산 - 정상 케이스")
    @Test
    void calculateDiscountRate_ValidCase() {
        // Given
        Long companySeq = 1L;
        List<CompanyDiscountHistory> histories = Arrays.asList(createDiscountHistory());

        given(discountHistoryMapper.findByCompanySeq(companySeq)).willReturn(histories);
        given(discountCalculator.calculateCurrentDiscountRate(eq(histories), any(LocalDate.class)))
            .willReturn(new BigDecimal("5.0"));

        // When
        BigDecimal result = discountService.calculateDiscountRate(companySeq);

        // Then
        assertThat(result).isEqualTo(new BigDecimal("5.0"));
        verify(discountHistoryMapper).findByCompanySeq(companySeq);
        verify(discountCalculator).calculateCurrentDiscountRate(eq(histories), any(LocalDate.class));
    }

    @DisplayName("회사별 현재 할인율 계산 - 이력 없음")
    @Test
    void calculateDiscountRate_NoHistory() {
        // Given
        Long companySeq = 1L;
        List<CompanyDiscountHistory> emptyHistories = Collections.emptyList();

        given(discountHistoryMapper.findByCompanySeq(companySeq)).willReturn(emptyHistories);
        given(discountCalculator.calculateCurrentDiscountRate(eq(emptyHistories), any(LocalDate.class)))
            .willReturn(BigDecimal.ZERO);

        // When
        BigDecimal result = discountService.calculateDiscountRate(companySeq);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("주문 금액에 할인 적용 - 정상 케이스")
    @Test
    void applyDiscount_ValidCase() {
        // Given
        Long companySeq = 1L;
        BigDecimal orderAmount = new BigDecimal("100000");
        BigDecimal discountRate = new BigDecimal("5.0");
        BigDecimal expectedDiscount = new BigDecimal("5000");

        given(discountHistoryMapper.findByCompanySeq(companySeq))
            .willReturn(Arrays.asList(createDiscountHistory()));
        given(discountCalculator.calculateCurrentDiscountRate(any(), any(LocalDate.class)))
            .willReturn(discountRate);
        given(discountCalculator.applyDiscount(orderAmount, discountRate))
            .willReturn(expectedDiscount);

        // When
        BigDecimal result = discountService.applyDiscount(orderAmount, companySeq);

        // Then
        assertThat(result).isEqualTo(expectedDiscount);
        verify(discountCalculator).applyDiscount(orderAmount, discountRate);
    }

    @DisplayName("최종 결제 금액 계산 - 정상 케이스")
    @Test
    void calculateFinalAmount_ValidCase() {
        // Given
        Long companySeq = 1L;
        BigDecimal orderAmount = new BigDecimal("100000");
        BigDecimal discountRate = new BigDecimal("5.0");
        BigDecimal discountAmount = new BigDecimal("5000");
        BigDecimal expectedFinalAmount = new BigDecimal("95000");

        given(discountHistoryMapper.findByCompanySeq(companySeq))
            .willReturn(Arrays.asList(createDiscountHistory()));
        given(discountCalculator.calculateCurrentDiscountRate(any(), any(LocalDate.class)))
            .willReturn(discountRate);
        given(discountCalculator.applyDiscount(orderAmount, discountRate))
            .willReturn(discountAmount);
        given(discountCalculator.calculateFinalAmount(orderAmount, discountAmount))
            .willReturn(expectedFinalAmount);

        // When
        BigDecimal result = discountService.calculateFinalAmount(orderAmount, companySeq);

        // Then
        assertThat(result).isEqualTo(expectedFinalAmount);
        verify(discountCalculator).calculateFinalAmount(orderAmount, discountAmount);
    }

    @DisplayName("전년도 구매액 조회 - 정상 케이스")
    @Test
    void getPreviousYearAmount_ValidCase() {
        // Given
        Long companySeq = 1L;
        String year = "2024";
        BigDecimal expectedAmount = new BigDecimal("150000000");

        given(summaryMapper.findPreviousYearTotalAmount(companySeq, year))
            .willReturn(expectedAmount);

        // When
        BigDecimal result = discountService.getPreviousYearAmount(companySeq, year);

        // Then
        assertThat(result).isEqualTo(expectedAmount);
        verify(summaryMapper).findPreviousYearTotalAmount(companySeq, year);
    }

    @DisplayName("할인 등급 평가 - 정상 케이스")
    @Test
    void evaluateDiscountGrade_ValidCase() {
        // Given
        Long companySeq = 1L;
        String year = "2024";
        BigDecimal previousYearAmount = new BigDecimal("200000000");
        DiscountCalculator.DiscountGrade expectedGrade = DiscountCalculator.DiscountGrade.PREMIUM;

        given(summaryMapper.findPreviousYearTotalAmount(companySeq, year))
            .willReturn(previousYearAmount);
        given(discountCalculator.evaluateDiscountGrade(previousYearAmount))
            .willReturn(expectedGrade);

        // When
        DiscountCalculator.DiscountGrade result = discountService.evaluateDiscountGrade(companySeq, year);

        // Then
        assertThat(result).isEqualTo(expectedGrade);
        verify(summaryMapper).findPreviousYearTotalAmount(companySeq, year);
        verify(discountCalculator).evaluateDiscountGrade(previousYearAmount);
    }

    @DisplayName("권장 할인율 조회 - 정상 케이스")
    @Test
    void getRecommendedDiscountRate_ValidCase() {
        // Given
        Long companySeq = 1L;
        String year = "2024";
        BigDecimal previousYearAmount = new BigDecimal("200000000");
        DiscountCalculator.DiscountGrade grade = DiscountCalculator.DiscountGrade.PREMIUM;
        BigDecimal expectedRate = new BigDecimal("6.0");

        given(summaryMapper.findPreviousYearTotalAmount(companySeq, year))
            .willReturn(previousYearAmount);
        given(discountCalculator.evaluateDiscountGrade(previousYearAmount))
            .willReturn(grade);
        given(discountCalculator.getRecommendedDiscountRate(grade))
            .willReturn(expectedRate);

        // When
        BigDecimal result = discountService.getRecommendedDiscountRate(companySeq, year);

        // Then
        assertThat(result).isEqualTo(expectedRate);
        verify(discountCalculator).getRecommendedDiscountRate(grade);
    }

    @DisplayName("전체 할인 프로세스 통합 테스트")
    @Test
    void fullDiscountProcess_IntegrationTest() {
        // Given
        Long companySeq = 1L;
        BigDecimal orderAmount = new BigDecimal("1000000");
        BigDecimal discountRate = new BigDecimal("6.0");
        BigDecimal discountAmount = new BigDecimal("60000");
        BigDecimal finalAmount = new BigDecimal("940000");

        given(discountHistoryMapper.findByCompanySeq(companySeq))
            .willReturn(Arrays.asList(createDiscountHistory()));
        given(discountCalculator.calculateCurrentDiscountRate(any(), any(LocalDate.class)))
            .willReturn(discountRate);
        given(discountCalculator.applyDiscount(orderAmount, discountRate))
            .willReturn(discountAmount);
        given(discountCalculator.calculateFinalAmount(orderAmount, discountAmount))
            .willReturn(finalAmount);

        // When - 할인율 조회
        BigDecimal currentRate = discountService.calculateDiscountRate(companySeq);

        // When - 할인 적용
        BigDecimal appliedDiscount = discountService.applyDiscount(orderAmount, companySeq);

        // When - 최종 금액 계산
        BigDecimal calculatedFinalAmount = discountService.calculateFinalAmount(orderAmount, companySeq);

        // Then
        assertThat(currentRate).isEqualTo(discountRate);
        assertThat(appliedDiscount).isEqualTo(discountAmount);
        assertThat(calculatedFinalAmount).isEqualTo(finalAmount);

        // 모든 메서드가 호출되었는지 검증
        verify(discountHistoryMapper, times(3)).findByCompanySeq(companySeq);
        verify(discountCalculator, times(3)).calculateCurrentDiscountRate(any(), any(LocalDate.class));
        verify(discountCalculator, times(2)).applyDiscount(any(), any());
        verify(discountCalculator, times(1)).calculateFinalAmount(any(), any());
    }

    private CompanyDiscountHistory createDiscountHistory() {
        CompanyDiscountHistory history = new CompanyDiscountHistory();
        history.setHistorySeq(1L);
        history.setCompanySeq(1L);
        history.setDiscountRate(new BigDecimal("5.0"));
        history.setEffectiveFrom(LocalDate.of(2025, 1, 1));
        history.setEffectiveTo(LocalDate.of(2025, 12, 31));
        return history;
    }
}