package io.goorm.board.config;

import io.goorm.board.exception.JwtAccessDeniedHandler;
import io.goorm.board.exception.JwtAuthenticationEntryPoint;
import io.goorm.board.filter.JwtAuthenticationFilter;
import io.goorm.board.service.JwtUserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
  private final JwtUserDetailsServiceImpl jwtUserDetailsService;

    // Fetch 전용 SecurityFilterChain
    @Bean
    @Order(1)
    public SecurityFilterChain fetchSecurityChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/fetch/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/fetch/auth/**").permitAll()
                        .requestMatchers("/fetch/**").authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Fetch authentication required for URL: {}", request.getRequestURI());
                            response.sendRedirect("/fetch/auth/login");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Fetch access denied for user: {} to URL: {}",
                                    request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                                    request.getRequestURI());
                            response.sendRedirect("/fetch/auth/login?error=access");
                        })
                )
                .formLogin(form -> form
                        .loginPage("/fetch/auth/login")
                        .loginProcessingUrl("/fetch/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/fetch/posts", true)
                        .failureUrl("/fetch/auth/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/fetch/auth/logout")
                        .logoutSuccessUrl("/fetch/auth/login?logout=true")
                        .permitAll()
                )
                .build();
    }

    // Axios 전용 SecurityFilterChain
    @Bean
    @Order(2)
    public SecurityFilterChain axiosSecurityChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/axios/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/axios/auth/**").permitAll()
                        .requestMatchers("/axios/**").authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Axios authentication required for URL: {}", request.getRequestURI());
                            response.sendRedirect("/axios/auth/login");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Axios access denied for user: {} to URL: {}",
                                    request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                                    request.getRequestURI());
                            response.sendRedirect("/axios/auth/login?error=access");
                        })
                )
                .formLogin(form -> form
                        .loginPage("/axios/auth/login")
                        .loginProcessingUrl("/axios/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/axios/post/list", true)
                        .failureUrl("/axios/auth/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/axios/auth/logout")
                        .logoutSuccessUrl("/axios/auth/login?logout=true")
                        .permitAll()
                )
                .build();
    }


  // JWT 페이지 전용 SecurityFilterChain (웹 페이지)
  @Bean
  @Order(3)
  public SecurityFilterChain jwtPagesSecurityChain(HttpSecurity http) throws Exception {
    return http
            .securityMatcher("/pages/**")
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/pages/**").permitAll()     // 모든 페이지 허용 (클라이언트에서 제어)
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                      log.warn("Pages authentication required for URL: {}", request.getRequestURI());
                      response.sendRedirect("/pages/auth/login");
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                      log.warn("Pages access denied for user: {} to URL: {}",
                              request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                              request.getRequestURI());
                      response.sendRedirect("/pages/auth/login?error=access");
                    }))
            .build();
  }

  // JWT API 전용 SecurityFilterChain
  @Bean
  @Order(4)
  public SecurityFilterChain jwtApiSecurityChain(HttpSecurity http) throws Exception {
    return http
            .securityMatcher("/jwt/**")
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/jwt/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/jwt/posts/**").permitAll()
                    .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler))
            .build();
  }

  // REST API 전용 SecurityFilterChain (세션 기반)
  @Bean
  @Order(5)
  public SecurityFilterChain apiSecurityChain(HttpSecurity http) throws Exception {
    return http
            .securityMatcher("/api/**")
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/*/auth/**").permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                      response.setStatus(401);
                      response.setContentType("application/json;charset=UTF-8");
                      io.goorm.board.dto.ErrorResponse errorResponse = io.goorm.board.dto.ErrorResponse.of("UNAUTHORIZED", "인증이 필요합니다", 401);
                      response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(errorResponse));
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                      response.setStatus(403);
                      response.setContentType("application/json;charset=UTF-8");
                      io.goorm.board.dto.ErrorResponse errorResponse = io.goorm.board.dto.ErrorResponse.of("ACCESS_DENIED", "접근 권한이 없습니다", 403);
                      response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(errorResponse));
                    }))
            .build();
  }

  // 기본 MVC SecurityFilterChain
  @Bean
  @Order(6)
  public SecurityFilterChain defaultSecurityChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(auth -> auth
                    // 기존 MVC 경로
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/buyer/**").hasRole("BUYER")
                    .requestMatchers("/", "/posts", "/auth/signup", "/auth/login").permitAll()
                    .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/favicon.*").permitAll()
                    .requestMatchers("/posts/[0-9]+").permitAll()
                    .requestMatchers("/posts/new", "/posts/*/edit", "/posts/*/delete").authenticated()
                    .requestMatchers("/auth/profile").authenticated()



                    // Swagger UI 경로
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                    .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
            )
            .exceptionHandling(ex -> ex
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                      log.warn("Access denied for user: {} to URL: {}",
                              request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                              request.getRequestURI());
                      response.sendRedirect("/error/403");
                    })
                    .authenticationEntryPoint((request, response, authException) -> {
                      log.warn("Authentication required for URL: {}", request.getRequestURI());
                      response.sendRedirect("/auth/login");
                    })
            )
            .formLogin(form -> form
                    .loginPage("/auth/login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .successHandler(authenticationSuccessHandler)
                    .failureUrl("/auth/login?error=true")
                    .permitAll()
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
      authenticationManagerBuilder
              .userDetailsService(jwtUserDetailsService)
              .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }
}