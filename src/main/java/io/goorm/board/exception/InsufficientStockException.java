package io.goorm.board.exception;

/**
 * 재고 부족 예외
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientStockException(String productName, int requestedQuantity, int availableQuantity) {
        super(String.format("재고가 부족합니다. 상품: %s, 요청수량: %d, 가용재고: %d",
                productName, requestedQuantity, availableQuantity));
    }
}