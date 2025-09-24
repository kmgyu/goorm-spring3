package io.goorm.board.dto.order;

import io.goorm.board.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 발주 상품 조회 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {

    private Long orderItemSeq;
    private Long orderSeq;
    private Long productSeq;
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDateTime createdAt;

    // 조인 정보
    private String productName;
    private String productCode;
    private String categoryName;

    /**
     * Entity -> DTO 변환
     */
    public static OrderItemDto from(OrderItem orderItem) {
        return OrderItemDto.builder()
                .orderItemSeq(orderItem.getOrderItemSeq())
                .orderSeq(orderItem.getOrderSeq())
                .productSeq(orderItem.getProductSeq())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .createdAt(orderItem.getCreatedAt())
                .productName(orderItem.getProductName())
                .productCode(orderItem.getProductCode())
                .categoryName(orderItem.getCategoryName())
                .build();
    }
}