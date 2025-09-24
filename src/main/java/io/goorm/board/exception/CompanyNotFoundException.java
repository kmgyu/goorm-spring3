package io.goorm.board.exception;

public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException() {
        super();
    }

    public CompanyNotFoundException(String message) {
        super(message);
    }
}