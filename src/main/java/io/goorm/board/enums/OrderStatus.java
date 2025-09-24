package io.goorm.board.enums;

/**
 * 주문 상태 Enum
 */
public enum OrderStatus {
    PENDING("대기중"),
    APPROVED("승인완료");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}