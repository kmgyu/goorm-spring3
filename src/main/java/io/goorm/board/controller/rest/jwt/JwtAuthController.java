package io.goorm.board.controller.rest.jwt;

import io.goorm.board.dto.ApiResponse;
import io.goorm.board.dto.LoginDto;
import io.goorm.board.dto.request.JwtRefreshRequest;
import io.goorm.board.dto.response.JwtLoginResponse;
import io.goorm.board.dto.response.JwtRefreshResponse;
import io.goorm.board.dto.response.UserResponse;
import io.goorm.board.entity.User;
import io.goorm.board.service.UserService;
import io.goorm.board.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/jwt/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API (JWT)", description = "JWT 기반 사용자 인증 관련 API")
public class JwtAuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "JWT 로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = JwtLoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtLoginResponse>> login(
            @Parameter(description = "로그인 정보") @Valid @RequestBody LoginDto loginRequest) {

        try {
            // Spring Security를 통한 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            // 인증된 사용자 정보 조회
            User user = userService.findByEmail(loginRequest.getEmail());

            // JWT 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            // 응답 생성
            JwtLoginResponse response = JwtLoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                    .user(UserResponse.from(user))
                    .build();

            ApiResponse<JwtLoginResponse> apiResponse = ApiResponse.<JwtLoginResponse>builder()
                    .success(true)
                    .message("로그인 성공")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("JWT 로그인 성공: {}", user.getEmail());
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("JWT 로그인 실패: {}", e.getMessage());

            ApiResponse<JwtLoginResponse> apiResponse = ApiResponse.<JwtLoginResponse>builder()
                    .success(false)
                    .message("로그인 실패: " + e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(401).body(apiResponse);
        }
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급받습니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = JwtRefreshResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 검증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtRefreshResponse>> refresh(
            @Parameter(description = "갱신 요청 정보") @Valid @RequestBody JwtRefreshRequest request) {

        try {
            String refreshToken = request.getRefreshToken();

            // Refresh Token 검증
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new RuntimeException("유효하지 않은 리프레시 토큰입니다");
            }

            // 토큰에서 사용자 이메일 추출
            String email = jwtUtil.getEmailFromToken(refreshToken);

            // 새로운 Access Token 생성
            String newAccessToken = jwtUtil.generateAccessToken(email);

            JwtRefreshResponse response = JwtRefreshResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                    .build();

            ApiResponse<JwtRefreshResponse> apiResponse = ApiResponse.<JwtRefreshResponse>builder()
                    .success(true)
                    .message("토큰 갱신 성공")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("JWT 토큰 갱신 성공: {}", email);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("JWT 토큰 갱신 실패: {}", e.getMessage());

            ApiResponse<JwtRefreshResponse> apiResponse = ApiResponse.<JwtRefreshResponse>builder()
                    .success(false)
                    .message("토큰 갱신 실패: " + e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(401).body(apiResponse);
        }
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 종료합니다 (클라이언트에서 토큰을 삭제하세요)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {

        try {
            // 현재 단계에서는 클라이언트에게 토큰 삭제를 안내
            // Day 2에서 Redis를 이용한 토큰 무효화 구현 예정

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("로그아웃되었습니다. 클라이언트에서 토큰을 삭제해주세요.")
                    .data("클라이언트에서 localStorage 또는 sessionStorage의 토큰을 삭제하세요.")
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("JWT 로그아웃: {}", user != null ? user.getEmail() : "unknown");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("JWT 로그아웃 실패: {}", e.getMessage());

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message("로그아웃 처리 중 오류가 발생했습니다.")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(500).body(response);
        }
    }
}