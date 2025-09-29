package io.goorm.board.controller.rest.json;

import io.goorm.board.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/json/auth")
@RequiredArgsConstructor
public class JsonAuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest,
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
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("sessionId", session.getId());
            response.put("user", Map.of(
                    "id", user.getUserSeq(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString()
            ));

        } catch (Exception e) {
            // 실패 응답
            response.put("success", false);
            response.put("message", "로그인 실패: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 세션 무효화
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // SecurityContext 클리어
            SecurityContextHolder.clearContext();

            response.put("success", true);
            response.put("message", "로그아웃 성공");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "로그아웃 실패: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/check")
    public Map<String, Object> checkSession(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                User user = (User) auth.getPrincipal();

                response.put("authenticated", true);
                response.put("sessionId", request.getSession().getId());
                response.put("user", Map.of(
                        "id", user.getUserSeq(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole().toString()
                ));
            } else {
                response.put("authenticated", false);
                response.put("message", "인증되지 않은 사용자");
            }

        } catch (Exception e) {
            response.put("authenticated", false);
            response.put("message", "세션 확인 실패: " + e.getMessage());
        }

        return response;
    }
}