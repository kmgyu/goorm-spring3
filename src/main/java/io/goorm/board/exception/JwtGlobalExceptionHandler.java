package io.goorm.board.exception;

import io.goorm.board.dto.ApiResponse;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@Slf4j
@ControllerAdvice(basePackages = "io.goorm.board.controller.jwt")
public class JwtGlobalExceptionHandler {

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleJwtException(JwtException e) {
        log.error("JWT 예외 발생: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("JWT 토큰 오류: " + e.getMessage())
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleExpiredJwtException(ExpiredJwtException e) {
        log.error("JWT 토큰 만료: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("토큰이 만료되었습니다.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleMalformedJwtException(MalformedJwtException e) {
        log.error("잘못된 JWT 토큰 형식: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("잘못된 토큰 형식입니다.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiResponse<Object>> handleSignatureException(SignatureException e) {
        log.error("JWT 서명 검증 실패: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("토큰 서명이 유효하지 않습니다.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnsupportedJwtException(UnsupportedJwtException e) {
        log.error("지원되지 않는 JWT 토큰: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("지원되지 않는 토큰입니다.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("잘못된 인자: {}", e.getMessage());

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(false)
                .message("잘못된 요청입니다.")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}