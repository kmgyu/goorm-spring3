package io.goorm.board.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 발주 상품 등록 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemCreateDto {

    @NotNull(message = "상품을 선택해주세요")
    private Long productSeq;

    @NotNull(message = "수량을 입력해주세요")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private Integer quantity;

    @NotNull(message = "단가를 입력해주세요")
    private BigDecimal unitPrice;
}