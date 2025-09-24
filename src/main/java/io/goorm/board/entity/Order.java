package io.goorm.board.entity;

import io.goorm.board.enums.DeliveryStatus;
import io.goorm.board.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 발주 엔티티
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

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
    private Long version; // 낙관적 락
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 조인 필드
    private String companyName;
    private String userName;
    private String userEmail;

    /**
     * 발주 승인 처리
     */
    public void approve(String approvedBy) {
        this.status = OrderStatus.APPROVED;
        this.deliveryStatus = DeliveryStatus.ORDER_COMPLETED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 배송 완료 처리
     */
    public void completeDelivery() {
        this.deliveryStatus = DeliveryStatus.DELIVERY_COMPLETED;
    }
}