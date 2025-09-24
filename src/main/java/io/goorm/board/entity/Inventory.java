package io.goorm.board.entity;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 재고 엔티티
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    private Long inventorySeq;
    private Long productSeq;
    private String location;
    private Integer currentStock;
    private Integer reservedStock;
    private Integer availableStock; // 계산 컬럼 (current_stock - reserved_stock)
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Integer reorderPoint;
    private LocalDateTime lastStockCheck;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdSeq;
    private Long updatedSeq;

    /**
     * 재고 부족 여부 확인
     */
    public boolean isLowStock() {
        return availableStock != null && minStockLevel != null &&
               availableStock <= minStockLevel;
    }

    /**
     * 재고 없음 여부 확인
     */
    public boolean isOutOfStock() {
        return availableStock == null || availableStock <= 0;
    }

    /**
     * 발주 필요 여부 확인
     */
    public boolean needsReorder() {
        return reorderPoint != null && availableStock != null &&
               availableStock <= reorderPoint;
    }

    /**
     * 주문 가능 수량 확인
     */
    public boolean canOrder(int quantity) {
        return !isOutOfStock() && availableStock >= quantity;
    }
}