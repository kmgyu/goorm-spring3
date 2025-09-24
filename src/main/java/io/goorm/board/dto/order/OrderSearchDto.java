package io.goorm.board.dto.order;

import io.goorm.board.dto.common.BaseSearchConditionDto;
import io.goorm.board.enums.DeliveryStatus;
import io.goorm.board.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 발주 검색 DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderSearchDto extends BaseSearchConditionDto {

    private String orderNumber;
    private Long companySeq;
    private OrderStatus status;
    private DeliveryStatus deliveryStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Override
    public boolean isEmpty() {
        return orderNumber == null || orderNumber.trim().isEmpty()
                && companySeq == null
                && status == null
                && deliveryStatus == null
                && startDate == null
                && endDate == null
                && !hasKeyword();
    }
}