package io.goorm.board.dto.product;

import io.goorm.board.enums.ProductStatus;
import io.goorm.board.enums.ProductUnit;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 정보 전송용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long productSeq;
    private String code;
    private String name;
    private String description;
    private Long categorySeq;
    private String categoryName;
    private Long supplierSeq;
    private String supplierName;
    private BigDecimal unitPrice;
    private BigDecimal unitCost;
    private ProductUnit unit;
    private String sku;
    private String barcode;
    private BigDecimal weight;
    private String dimensions;
    private String imageUrl;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdSeq;
    private Long updatedSeq;

    // 계산된 필드들
    private BigDecimal marginAmount;
    private BigDecimal marginRate;

    // 재고 관련 필드
    private Integer stock;

    /**
     * 마진액 계산
     */
    public BigDecimal getMarginAmount() {
        if (unitPrice == null || unitCost == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.subtract(unitCost);
    }

    /**
     * 마진율 계산 (백분율)
     */
    public BigDecimal getMarginRate() {
        if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getMarginAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(unitCost, 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 판매 가능 여부
     */
    public boolean isSellable() {
        return status != null && status.isSellable();
    }

    /**
     * 활성 상태 여부
     */
    public boolean isActive() {
        return status != null && status.isActive();
    }

    /**
     * 이미지 존재 여부
     */
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    /**
     * 가격 검증
     */
    public boolean isPriceValid() {
        if (unitPrice == null || unitCost == null) {
            return false;
        }
        return unitPrice.compareTo(unitCost) >= 0;
    }
}