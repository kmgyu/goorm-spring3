package io.goorm.board.enums;

/**
 * 배송 상태 Enum
 */
public enum DeliveryStatus {
    ORDER_COMPLETED("주문완료"),
    DELIVERY_COMPLETED("배송완료");

    private final String displayName;

    DeliveryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}