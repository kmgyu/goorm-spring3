package io.goorm.board.exception;

/**
 * 입금 완료 처리 관련 예외
 */
public class PaymentCompleteException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public PaymentCompleteException(String messageKey, Object... args) {
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