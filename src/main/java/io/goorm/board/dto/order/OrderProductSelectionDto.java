package io.goorm.board.dto.order;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 주문 상품 선택 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductSelectionDto {

    private boolean selected;
    private Long productSeq;
    private Integer quantity;
}