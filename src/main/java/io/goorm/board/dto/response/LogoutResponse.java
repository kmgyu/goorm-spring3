package io.goorm.board.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogoutResponse {
    private boolean success;
    private String message;

    public static LogoutResponse success() {
        return LogoutResponse.builder()
                .success(true)
                .build();
    }
}