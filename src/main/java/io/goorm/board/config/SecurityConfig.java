package io.goorm.board.config;

import io.goorm.board.enums.UserRole;
import io.goorm.board.auth.AuthFailureHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthFailureHandler authFailureHandler;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/api/**")  // API 경로는 CSRF 비활성화
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole(UserRole.ADMIN.name())
                .requestMatchers("/buyer/orders/*/invoice").hasAnyRole(UserRole.ADMIN.name(), UserRole.BUYER.name())
                .requestMatchers("/buyer/**").hasRole(UserRole.BUYER.name())

                    // REST API 경로
                .requestMatchers("/api/*/auth/**").permitAll()  // 모든 API 인증 엔드포인트 공개
                .requestMatchers("/api/**").authenticated()     // 나머지 API는 인증 필요

                .requestMatchers("/", "/posts", "/auth/signup", "/auth/login").permitAll()
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/favicon.*").permitAll()
                .requestMatchers("/posts/[0-9]+", "/error/**").permitAll()
                .requestMatchers("/posts/new", "/posts/*/edit", "/posts/*/delete").authenticated()
                .requestMatchers("/auth/profile").authenticated()
                .anyRequest().authenticated()
            )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Access denied for user: {} to URL: {}",
                                    request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                                    request.getRequestURI());

                            // REST API 경로는 JSON 응답
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(403);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"ACCESS_DENIED\",\"message\":\"접근 권한이 없습니다.\",\"status\":403,\"timestamp\":\"" + java.time.LocalDateTime.now() + "\"}}");
                            } else {
                                response.sendRedirect("/error/403");
                            }
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Authentication required for URL: {}", request.getRequestURI());

                            // REST API 경로는 JSON 응답
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(401);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\",\"status\":401,\"timestamp\":\"" + java.time.LocalDateTime.now() + "\"}}");
                            } else {
                                response.sendRedirect("/auth/login");
                            }
                        })
                )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authFailureHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        return authenticationManagerBuilder.build();
    }
}