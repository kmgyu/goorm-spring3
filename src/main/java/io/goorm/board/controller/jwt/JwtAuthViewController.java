package io.goorm.board.controller.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/pages/jwt/auth")
@RequiredArgsConstructor
public class JwtAuthViewController {

    @GetMapping("/login")
    public String loginPage() {
        return "jwt/auth/login";
    }

    @GetMapping("/tokens")
    public String tokensPage() {
        return "jwt/auth/tokens";
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