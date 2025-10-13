package io.goorm.board.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtRefreshResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
}