package io.goorm.board.enums;

public enum UserRole {
    ADMIN("관리자"),
    BUYER("바이어");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // Spring Security에서 사용할 권한명 (ROLE_ 접두어)
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}