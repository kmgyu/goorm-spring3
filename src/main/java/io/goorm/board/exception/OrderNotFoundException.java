package io.goorm.board.exception;

/**
 * 발주 없음 예외
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException() {
        super();
    }

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderNotFoundException(Long orderSeq) {
        super("발주를 찾을 수 없습니다. ID: " + orderSeq);
    }
}