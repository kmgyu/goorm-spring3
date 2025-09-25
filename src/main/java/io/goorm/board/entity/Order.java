package io.goorm.board.entity;

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

    // 입금 관련 필드
    private PaymentStatus paymentStatus;
    private LocalDate paymentDueDate;            // 입금예정일 (다음달 말일)
    private LocalDateTime paymentCompletedDate;  // 실제 입금일
    private LocalDateTime invoiceGeneratedAt;    // 인보이스 확정일 (배송완료시)
    private Long paymentCompletedBySeq;          // 입금 완료 처리자 ID
    private String paymentCompletedBy;           // 입금 완료 처리자명

    // 배송 완료 관련 필드
    private Long deliveryCompletedBySeq;         // 배송 완료 처리자 ID
    private String deliveryCompletedBy;          // 배송 완료 처리자명
    private LocalDateTime deliveryCompletedAt;   // 배송 완료 시점

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
    public void completeDelivery(Long deliveryCompletedBySeq, String completedBy) {
        this.status = OrderStatus.COMPLETED;
        this.deliveryStatus = DeliveryStatus.DELIVERY_COMPLETED;
        this.deliveryCompletedBySeq = deliveryCompletedBySeq;
        this.deliveryCompletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 배송 완료와 동시에 인보이스 확정
        confirmInvoice();

        if (this.notes == null) {
            this.notes = "배송 완료: " + completedBy;
        } else {
            this.notes += " | 배송 완료: " + completedBy;
        }
    }


    /**
     * 배송 시작 처리
     */
    public void startDelivery(String startedBy) {
        this.status = OrderStatus.APPROVED;
        this.deliveryStatus = DeliveryStatus.ORDER_COMPLETED;
        this.updatedAt = LocalDateTime.now();
        if (this.notes == null) {
            this.notes = "배송 시작: " + startedBy;
        } else {
            this.notes += " | 배송 시작: " + startedBy;
        }
    }

    /**
     * 인보이스 확정 처리 (배송 완료 시 호출)
     */
    private void confirmInvoice() {
        this.invoiceGeneratedAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.PENDING;

        // 다음달 말일을 입금예정일로 설정
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        this.paymentDueDate = nextMonth.withDayOfMonth(nextMonth.lengthOfMonth());
    }

    /**
     * 입금 완료 처리
     */
    public void completePayment(Long paymentCompletedBySeq, String completedBy) {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.paymentCompletedDate = LocalDateTime.now();
        this.paymentCompletedBySeq = paymentCompletedBySeq;
        this.paymentCompletedBy = completedBy;
        this.updatedAt = LocalDateTime.now();

        if (this.notes == null) {
            this.notes = "입금 완료: " + completedBy;
        } else {
            this.notes += " | 입금 완료: " + completedBy;
        }
    }

    /**
     * 연체 여부 확인
     */
    public boolean isOverdue() {
        return paymentStatus == PaymentStatus.PENDING
               && paymentDueDate != null
               && paymentDueDate.isBefore(LocalDate.now());
    }

    /**
     * 표시용 결제 상태 (연체 포함)
     */
    public String getDisplayPaymentStatus() {
        if (isOverdue()) {
            return "연체";
        }
        return paymentStatus != null ? paymentStatus.getDisplayName() : "미설정";
    }
}