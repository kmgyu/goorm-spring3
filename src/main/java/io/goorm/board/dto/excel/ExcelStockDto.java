package io.goorm.board.dto.excel;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 엑셀 재고 처리 DTO
 */
@Data
public class ExcelStockDto {

    @NotBlank(message = "상품코드는 필수입니다")
    private String productCode;

    private String productName; // 참조용 (실제 매칭에는 미사용)

    @Deprecated // 더 이상 사용하지 않음
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