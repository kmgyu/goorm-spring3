package io.goorm.board.enums;

/**
 * 재고 거래 유형
 */
public enum TransactionType {
    RECEIVING("입고", "재고 입고 처리"),
    ORDER_CONSUMED("출고", "주문에 의한 재고 소모");

    private final String displayName;
    private final String description;

    TransactionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}