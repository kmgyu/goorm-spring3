package io.goorm.board.enums;

/**
 * 입금 상태 열거형
 */
public enum PaymentStatus {
    PENDING("입금대기"),
    COMPLETED("입금완료");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}