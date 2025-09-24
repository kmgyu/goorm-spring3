package io.goorm.board.exception;

public class InvalidUserRoleException extends RuntimeException {
    public InvalidUserRoleException() {
        super();
    }

    public InvalidUserRoleException(String message) {
        super(message);
    }
}