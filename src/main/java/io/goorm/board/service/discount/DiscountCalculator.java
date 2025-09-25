package io.goorm.board.service.discount;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 할인율 계산을 담당하는 순수 자바 클래스
 * 외부 의존성 없이 비즈니스 로직만 처리
 */
@Component
@Slf4j
public class DiscountCalculator {

    /**
     * 주문 금액에 할인율 적용
     */
    public BigDecimal applyDiscount(BigDecimal orderAmount, BigDecimal discountRate) {
        if (orderAmount == null || discountRate == null) {
            return BigDecimal.ZERO;
        }

        if (orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (discountRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 할인율이 100%를 초과하지 않도록 제한
        BigDecimal effectiveRate = discountRate.min(new BigDecimal("100"));

        return orderAmount.multiply(effectiveRate)
                .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 최종 결제 금액 계산 (원금 - 할인금액)
     */
    public BigDecimal calculateFinalAmount(BigDecimal orderAmount, BigDecimal discountAmount) {
        if (orderAmount == null) {
            return BigDecimal.ZERO;
        }

        if (discountAmount == null) {
            return orderAmount;
        }

        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        // 최종 금액이 음수가 되지 않도록 보장
        return finalAmount.max(BigDecimal.ZERO);
    }

    /**
     * 전년도 구매액 기준 할인율 등급 판정
     */
    public DiscountGrade evaluateDiscountGrade(BigDecimal previousYearAmount) {
        if (previousYearAmount == null || previousYearAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return DiscountGrade.NONE;
        }

        // 1억 5천만원 이상: 프리미엄
        if (previousYearAmount.compareTo(new BigDecimal("150000000")) >= 0) {
            return DiscountGrade.PREMIUM;
        }

        // 8천만원 이상: 골드
        if (previousYearAmount.compareTo(new BigDecimal("80000000")) >= 0) {
            return DiscountGrade.GOLD;
        }

        // 3천만원 이상: 실버
        if (previousYearAmount.compareTo(new BigDecimal("30000000")) >= 0) {
            return DiscountGrade.SILVER;
        }

        // 1천만원 이상: 브론즈
        if (previousYearAmount.compareTo(new BigDecimal("10000000")) >= 0) {
            return DiscountGrade.BRONZE;
        }

        return DiscountGrade.BASIC;
    }

    /**
     * 할인율 등급별 권장 할인율 계산
     */
    public BigDecimal getRecommendedDiscountRate(DiscountGrade grade) {
        switch (grade) {
            case PREMIUM:
                return new BigDecimal("6.0");
            case GOLD:
                return new BigDecimal("5.0");
            case SILVER:
                return new BigDecimal("3.0");
            case BRONZE:
                return new BigDecimal("2.0");
            case BASIC:
                return new BigDecimal("1.0");
            case NONE:
            default:
                return BigDecimal.ZERO;
        }
    }


    /**
     * 할인율 등급 enum
     */
    public enum DiscountGrade {
        PREMIUM("프리미엄", "1억 5천만원 이상"),
        GOLD("골드", "8천만원 이상"),
        SILVER("실버", "3천만원 이상"),
        BRONZE("브론즈", "1천만원 이상"),
        BASIC("기본", "1천만원 미만"),
        NONE("없음", "구매 이력 없음");

        private final String displayName;
        private final String description;

        DiscountGrade(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }
}