package io.goorm.board.exception;

/**
 * 배송 완료 처리 관련 예외
 */
public class DeliveryCompleteException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public DeliveryCompleteException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}