package io.goorm.board.exception;

public class OrderItemsNotSelectedException extends RuntimeException {
    public OrderItemsNotSelectedException() {
        super();
    }

    public OrderItemsNotSelectedException(String message) {
        super(message);
    }
}