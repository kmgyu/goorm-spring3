package io.goorm.board.dto.order;

import io.goorm.board.entity.Order;
import io.goorm.board.enums.DeliveryStatus;
import io.goorm.board.enums.OrderStatus;
import io.goorm.board.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 발주 조회 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

    private Long orderSeq;
    private Long companySeq;
    private Long userSeq;
    private String orderNumber;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private DeliveryStatus deliveryStatus;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private BigDecimal discountRate;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String notes;
    private LocalDateTime createdAt;

    // 결제 및 인보이스 관련 필드
    private PaymentStatus paymentStatus;
    private LocalDate paymentDueDate;
    private LocalDateTime paymentCompletedDate;
    private LocalDateTime invoiceGeneratedAt;
    private Long paymentCompletedBySeq;
    private String paymentCompletedBy;
    private Long deliveryCompletedBySeq;
    private String deliveryCompletedBy;
    private LocalDateTime deliveryCompletedAt;

    // 조인 정보
    private String companyName;
    private String userName;
    private String userEmail;

    // 발주 상품 목록
    private List<OrderItemDto> orderItems;

    /**
     * Entity -> DTO 변환
     */
    public static OrderDto from(Order order) {
        return OrderDto.builder()
                .orderSeq(order.getOrderSeq())
                .companySeq(order.getCompanySeq())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .deliveryStatus(order.getDeliveryStatus())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .discountRate(order.getDiscountRate())
                .approvedBy(order.getApprovedBy())
                .approvedAt(order.getApprovedAt())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .paymentStatus(order.getPaymentStatus())
                .paymentDueDate(order.getPaymentDueDate())
                .paymentCompletedDate(order.getPaymentCompletedDate())
                .invoiceGeneratedAt(order.getInvoiceGeneratedAt())
                .paymentCompletedBySeq(order.getPaymentCompletedBySeq())
                .paymentCompletedBy(order.getPaymentCompletedBy())
                .deliveryCompletedBySeq(order.getDeliveryCompletedBySeq())
                .deliveryCompletedBy(order.getDeliveryCompletedBy())
                .deliveryCompletedAt(order.getDeliveryCompletedAt())
                .companyName(order.getCompanyName())
                .userName(order.getUserName())
                .userEmail(order.getUserEmail())
                .build();
    }
}