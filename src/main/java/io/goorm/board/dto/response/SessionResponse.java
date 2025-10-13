package io.goorm.board.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionResponse {
    private boolean authenticated;
    private String sessionId;
    private UserResponse user;
    private String message;

    public static SessionResponse of(boolean authenticated, String sessionId, UserResponse user) {
        return SessionResponse.builder()
                .authenticated(authenticated)
                .sessionId(sessionId)
                .user(user)
                .build();
    }
}