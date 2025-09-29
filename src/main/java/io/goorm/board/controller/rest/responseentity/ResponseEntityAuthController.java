package io.goorm.board.controller.rest.responseentity;

import io.goorm.board.dto.ApiResponse;
import io.goorm.board.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/responseentity/auth")
@RequiredArgsConstructor
public class ResponseEntityAuthController {
    // todo : i18n message refactor
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> loginRequest,
                                                                  HttpServletRequest request) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Map<String, Object> response = new HashMap<>();

        try {
            // 인증 처리
            Authentication authToken = new UsernamePasswordAuthenticationToken(email, password);
            Authentication authentication = authenticationManager.authenticate(authToken);

            // SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 세션에 SecurityContext 저장
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            // 사용자 정보 가져오기
            User user = (User) authentication.getPrincipal();

            // 성공 응답
            Map<String, Object> data = Map.of(
                    "sessionId", session.getId(),
                    "user", Map.of(
                            "id", user.getUserSeq(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "role", user.getRole().toString()
                    )
            );
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("로그인 성공", data);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            // 실패 응답은 GlobalExceptionHandler에서 처리
            throw new RuntimeException("로그인 실패: " + e.getMessage(), e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 세션 무효화
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // SecurityContext 클리어
            SecurityContextHolder.clearContext();

            ApiResponse<Void> apiResponse = ApiResponse.success("로그아웃 성공");
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            // 실패 응답은 GlobalExceptionHandler에서 처리
            throw new RuntimeException("로그아웃 실패: " + e.getMessage(), e);
        }
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSession(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                User user = (User) auth.getPrincipal();

                Map<String, Object> data = Map.of(
                        "authenticated", true,
                        "sessionId", request.getSession().getId(),
                        "user", Map.of(
                                "id", user.getUserSeq(),
                                "username", user.getUsername(),
                                "email", user.getEmail(),
                                "role", user.getRole().toString()
                        )
                );
                ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success("세션 확인 성공", data);
                return ResponseEntity.ok(apiResponse);
            } else {
                throw new RuntimeException("인증되지 않은 사용자");
            }

        } catch (Exception e) {
            // 실패 응답은 GlobalExceptionHandler에서 처리
            throw new RuntimeException("세션 확인 실패: " + e.getMessage(), e);
        }
    }
}