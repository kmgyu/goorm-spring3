package io.goorm.board.service.discount;

import io.goorm.board.entity.CompanyDiscountHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("할인 계산기 테스트")
class DiscountCalculatorTest {

    private DiscountCalculator discountCalculator;

    @BeforeEach
    void setUp() {
        discountCalculator = new DiscountCalculator();
    }

    @DisplayName("현재 유효한 할인율 계산 - 정상 케이스")
    @Test
    void calculateCurrentDiscountRate_ValidCase() {
        // Given
        LocalDate currentDate = LocalDate.of(2025, 6, 15);
        List<CompanyDiscountHistory> histories = Arrays.asList(
            createDiscountHistory(new BigDecimal("5.0"),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)),
            createDiscountHistory(new BigDecimal("3.0"),
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
        );

        // When
        BigDecimal result = discountCalculator.calculateCurrentDiscountRate(histories, currentDate);

        // Then
        assertThat(result).isEqualTo(new BigDecimal("5.0"));
    }

    @DisplayName("현재 유효한 할인율 계산 - 빈 리스트")
    @Test
    void calculateCurrentDiscountRate_EmptyList() {
        // Given
        LocalDate currentDate = LocalDate.of(2025, 6, 15);
        List<CompanyDiscountHistory> histories = Collections.emptyList();

        // When
        BigDecimal result = discountCalculator.calculateCurrentDiscountRate(histories, currentDate);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("현재 유효한 할인율 계산 - 유효한 기간 없음")
    @Test
    void calculateCurrentDiscountRate_NoValidPeriod() {
        // Given
        LocalDate currentDate = LocalDate.of(2025, 6, 15);
        List<CompanyDiscountHistory> histories = Arrays.asList(
            createDiscountHistory(new BigDecimal("5.0"),
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31))
        );

        // When
        BigDecimal result = discountCalculator.calculateCurrentDiscountRate(histories, currentDate);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("할인 적용 - 정상 케이스")
    @Test
    void applyDiscount_ValidCase() {
        // Given
        BigDecimal orderAmount = new BigDecimal("100000");
        BigDecimal discountRate = new BigDecimal("5.0");

        // When
        BigDecimal result = discountCalculator.applyDiscount(orderAmount, discountRate);

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @DisplayName("할인 적용 - 100% 초과 할인율 제한")
    @Test
    void applyDiscount_ExcessiveRate() {
        // Given
        BigDecimal orderAmount = new BigDecimal("100000");
        BigDecimal discountRate = new BigDecimal("150.0");

        // When
        BigDecimal result = discountCalculator.applyDiscount(orderAmount, discountRate);

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("100000.00")); // 100% 할인
    }

    @DisplayName("할인 적용 - null 값 처리")
    @Test
    void applyDiscount_NullValues() {
        // When & Then
        assertThat(discountCalculator.applyDiscount(null, new BigDecimal("5.0")))
            .isEqualTo(BigDecimal.ZERO);

        assertThat(discountCalculator.applyDiscount(new BigDecimal("100000"), null))
            .isEqualTo(BigDecimal.ZERO);

        assertThat(discountCalculator.applyDiscount(null, null))
            .isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("최종 금액 계산 - 정상 케이스")
    @Test
    void calculateFinalAmount_ValidCase() {
        // Given
        BigDecimal orderAmount = new BigDecimal("100000");
        BigDecimal discountAmount = new BigDecimal("5000");

        // When
        BigDecimal result = discountCalculator.calculateFinalAmount(orderAmount, discountAmount);

        // Then
        assertThat(result).isEqualTo(new BigDecimal("95000"));
    }

    @DisplayName("최종 금액 계산 - 음수 방지")
    @Test
    void calculateFinalAmount_PreventNegative() {
        // Given
        BigDecimal orderAmount = new BigDecimal("50000");
        BigDecimal discountAmount = new BigDecimal("70000");

        // When
        BigDecimal result = discountCalculator.calculateFinalAmount(orderAmount, discountAmount);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("할인 등급 평가 - 프리미엄")
    @Test
    void evaluateDiscountGrade_Premium() {
        // Given
        BigDecimal amount = new BigDecimal("200000000"); // 2억원

        // When
        DiscountCalculator.DiscountGrade result = discountCalculator.evaluateDiscountGrade(amount);

        // Then
        assertThat(result).isEqualTo(DiscountCalculator.DiscountGrade.PREMIUM);
    }

    @DisplayName("할인 등급 평가 - 골드")
    @Test
    void evaluateDiscountGrade_Gold() {
        // Given
        BigDecimal amount = new BigDecimal("100000000"); // 1억원

        // When
        DiscountCalculator.DiscountGrade result = discountCalculator.evaluateDiscountGrade(amount);

        // Then
        assertThat(result).isEqualTo(DiscountCalculator.DiscountGrade.GOLD);
    }

    @DisplayName("할인 등급 평가 - 실버")
    @Test
    void evaluateDiscountGrade_Silver() {
        // Given
        BigDecimal amount = new BigDecimal("50000000"); // 5천만원

        // When
        DiscountCalculator.DiscountGrade result = discountCalculator.evaluateDiscountGrade(amount);

        // Then
        assertThat(result).isEqualTo(DiscountCalculator.DiscountGrade.SILVER);
    }

    @DisplayName("할인 등급 평가 - 브론즈")
    @Test
    void evaluateDiscountGrade_Bronze() {
        // Given
        BigDecimal amount = new BigDecimal("20000000"); // 2천만원

        // When
        DiscountCalculator.DiscountGrade result = discountCalculator.evaluateDiscountGrade(amount);

        // Then
        assertThat(result).isEqualTo(DiscountCalculator.DiscountGrade.BRONZE);
    }

    @DisplayName("할인 등급 평가 - 기본")
    @Test
    void evaluateDiscountGrade_Basic() {
        // Given
        BigDecimal amount = new BigDecimal("5000000"); // 5백만원

        // When
        DiscountCalculator.DiscountGrade result = discountCalculator.evaluateDiscountGrade(amount);

        // Then
        assertThat(result).isEqualTo(DiscountCalculator.DiscountGrade.BASIC);
    }

    @DisplayName("할인 등급 평가 - 없음")
    @Test
    void evaluateDiscountGrade_None() {
        // When & Then
        assertThat(discountCalculator.evaluateDiscountGrade(null))
            .isEqualTo(DiscountCalculator.DiscountGrade.NONE);

        assertThat(discountCalculator.evaluateDiscountGrade(BigDecimal.ZERO))
            .isEqualTo(DiscountCalculator.DiscountGrade.NONE);

        assertThat(discountCalculator.evaluateDiscountGrade(new BigDecimal("-1000")))
            .isEqualTo(DiscountCalculator.DiscountGrade.NONE);
    }

    @DisplayName("권장 할인율 조회 - 모든 등급")
    @Test
    void getRecommendedDiscountRate_AllGrades() {
        // When & Then
        assertThat(discountCalculator.getRecommendedDiscountRate(DiscountCalculator.DiscountGrade.PREMIUM))
            .isEqualTo(new BigDecimal("6.0"));

        assertThat(discountCalculator.getRecommendedDiscountRate(DiscountCalculator.DiscountGrade.GOLD))
            .isEqualTo(new BigDecimal("5.0"));

        assertThat(discountCalculator.getRecommendedDiscountRate(DiscountCalculator.DiscountGrade.SILVER))
            .isEqualTo(new BigDecimal("3.0"));

        assertThat(discountCalculator.getRecommendedDiscountRate(DiscountCalculator.DiscountGrade.BRONZE))
            .isEqualTo(new BigDecimal("2.0"));

        assertThat(discountCalculator.getRecommendedDiscountRate(DiscountCalculator.DiscountGrade.BASIC))
            .isEqualTo(new BigDecimal("1.0"));

        assertThat(discountCalculator.getRecommendedDiscountRate(DiscountCalculator.DiscountGrade.NONE))
            .isEqualTo(BigDecimal.ZERO);
    }

    @DisplayName("종합 시나리오 테스트 - 전체 할인 프로세스")
    @Test
    void fullDiscountProcess_IntegrationTest() {
        // Given - 주문금액 100만원, 할인율 5%
        BigDecimal orderAmount = new BigDecimal("1000000");
        BigDecimal discountRate = new BigDecimal("5.0");

        // When - 할인 적용
        BigDecimal discountAmount = discountCalculator.applyDiscount(orderAmount, discountRate);
        BigDecimal finalAmount = discountCalculator.calculateFinalAmount(orderAmount, discountAmount);

        // Then
        assertThat(discountAmount).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(finalAmount).isEqualByComparingTo(new BigDecimal("950000"));
    }

    private CompanyDiscountHistory createDiscountHistory(BigDecimal discountRate, LocalDate from, LocalDate to) {
        CompanyDiscountHistory history = new CompanyDiscountHistory();
        history.setDiscountRate(discountRate);
        history.setEffectiveFrom(from);
        history.setEffectiveTo(to);
        return history;
    }
}