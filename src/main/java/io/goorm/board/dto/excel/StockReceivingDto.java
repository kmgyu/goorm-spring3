package io.goorm.board.dto.excel;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 엑셀 입고 처리 DTO
 */
@Data
public class StockReceivingDto {

    @NotBlank(message = "상품명은 필수입니다")
    private String productName;

    @NotBlank(message = "카테고리명은 필수입니다")
    private String categoryName;

    @NotNull(message = "입고수량은 필수입니다")
    @Min(value = 1, message = "입고수량은 1개 이상이어야 합니다")
    private Integer quantity;

    @NotNull(message = "입고단가는 필수입니다")
    @Min(value = 0, message = "입고단가는 0원 이상이어야 합니다")
    private BigDecimal unitPrice;

    private String note;

    // 엑셀 파싱용 필드
    private int rowNumber;
    private Long productSeq;
}