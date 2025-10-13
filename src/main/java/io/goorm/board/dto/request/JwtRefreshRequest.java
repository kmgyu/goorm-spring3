package io.goorm.board.dto.request;

import lombok.Data;

@Data
public class JwtRefreshRequest {
    private String refreshToken;
}