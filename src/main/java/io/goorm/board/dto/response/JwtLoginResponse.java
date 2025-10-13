package io.goorm.board.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtLoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;
}