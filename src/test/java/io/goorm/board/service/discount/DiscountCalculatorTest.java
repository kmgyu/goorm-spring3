package io.goorm.board.service.discount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("할인 계산기 테스트")
class DiscountCalculatorTest {

    private DiscountCalculator discountCalculator;

    @BeforeEach
    void setUp() {
        discountCalculator = new DiscountCalculator();
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

    @DisplayName("할인 등급 평가 - 없음")
    @Test
    void evaluateDiscountGrade_None() {
        // When & Then
        assertThat(discountCalculator.evaluateDiscountGrade(null))
            .isEqualTo(DiscountCalculator.DiscountGrade.NONE);

        assertThat(discountCalculator.evaluateDiscountGrade(BigDecimal.ZERO))
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

        assertThat(discountCalculator.getRecommendedDiscountRate(DiscountCalculator.DiscountGrade.NONE))
            .isEqualTo(BigDecimal.ZERO);
    }
}