package io.goorm.board.exception;

/**
 * 할인 오류 예외
 */
public class InvalidDiscountException extends RuntimeException {

    public InvalidDiscountException(String message) {
        super(message);
    }

    public InvalidDiscountException(String message, Throwable cause) {
        super(message, cause);
    }
}