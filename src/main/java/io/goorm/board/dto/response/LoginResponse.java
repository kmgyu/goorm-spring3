package io.goorm.board.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String sessionId;
    private UserResponse user;
    private String message;

    public static LoginResponse of(String sessionId, UserResponse user) {
        return LoginResponse.builder()
                .sessionId(sessionId)
                .user(user)
                .build();
    }
}