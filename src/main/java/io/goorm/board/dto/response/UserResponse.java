package io.goorm.board.dto.response;

import io.goorm.board.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getUserSeq())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .build();
    }
}