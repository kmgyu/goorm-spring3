package io.goorm.board.controller.jwt;

import io.goorm.board.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequestMapping("/jwt/auth")
@RequiredArgsConstructor
@Slf4j
public class JwtAuthViewController {

    private final JwtUtil jwtUtil;

    @GetMapping("/login")
    public String loginPage() {
        return "jwt/auth/login";
    }

    @GetMapping("/tokens")
    public String tokensPage() {
        return "jwt/auth/tokens";
    }

    @GetMapping("/check")
    @ResponseBody
    public ResponseEntity<?> checkToken(@CookieValue(value = "accessToken", required = false) String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.ok(Map.of("success", true, "data", Map.of("valid", false)));
        }

        boolean valid = jwtUtil.validateToken(accessToken);
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of("valid", valid)));
    }

    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<?> getUserInfo(@CookieValue(value = "accessToken", required = false) String accessToken) {
        if (accessToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        var emails = jwtUtil.getEmailFromToken(accessToken);
        return ResponseEntity.ok(Map.of("email", emails));
    }


    /**
     * httpOnly Cookie 기반 JWT 정보 반환
     * - AccessToken / RefreshToken 쿠키를 읽고
     * - 유효하다면 subject(email)과 만료시간을 JSON 형태로 반환
     */
    @GetMapping("/cookie-info")
    public ResponseEntity<?> getCookieInfo(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of("accessToken", null, "refreshToken", null)
            ));
        }

        String accessToken = null;
        String refreshToken = null;

        // 쿠키에서 토큰 추출
        for (Cookie cookie : cookies) {
            if ("accessToken".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            } else if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }

        Map<String, Object> accessTokenInfo = buildTokenInfo(accessToken);
        Map<String, Object> refreshTokenInfo = buildTokenInfo(refreshToken);

        Map<String, Object> data = Map.of(
                "accessToken", accessTokenInfo,
                "refreshToken", refreshTokenInfo
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data
        ));
    }

    /**
     * 토큰 유효성 검사 및 세부 정보 구성
     */
    private Map<String, Object> buildTokenInfo(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("JWT 토큰이 유효하지 않음");
                return null;
            }

            var claims = jwtUtil.getClaims(token);
            var expiry = claims.getExpiration()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            return Map.of(
                    "user", claims.getSubject(),
                    "expiry", expiry.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

        } catch (Exception e) {
            log.error("JWT 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    // Cookie 방식을 위한 추가 엔드포인트
    @PostMapping("/set-cookie-tokens")
    @ResponseBody
    public ResponseEntity<?> setCookieTokens(
            @RequestBody TokenRequest request,
            HttpServletResponse response) {

        // httpOnly Cookie 설정
        Cookie accessTokenCookie = new Cookie("accessToken", request.getAccessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setMaxAge(15 * 60); // 15분
        accessTokenCookie.setPath("/");

        Cookie refreshTokenCookie = new Cookie("refreshToken", request.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        refreshTokenCookie.setPath("/");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok().build();
    }

    // Cookie 토큰 삭제
    @PostMapping("/clear-cookie-tokens")
    @ResponseBody
    public ResponseEntity<?> clearCookieTokens(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", "");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");

        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok().build();
    }

    // Token 요청 DTO
    public static class TokenRequest {
        private String accessToken;
        private String refreshToken;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}