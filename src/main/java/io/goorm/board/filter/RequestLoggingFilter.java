package io.goorm.board.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 모든 HTTP 요청을 로깅하는 필터
 */
@Slf4j
@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 정적 리소스는 로깅 제외
        String requestURI = httpRequest.getRequestURI();
        if (isStaticResource(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 요청별 고유 ID 생성
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        long startTime = System.currentTimeMillis();

        // 요청 정보 로깅
        String method = httpRequest.getMethod();
        String queryString = httpRequest.getQueryString();
        String userAgent = httpRequest.getHeader("User-Agent");
        String remoteAddr = getClientIP(httpRequest);

        log.info("[{}] {} {} {} - User-Agent: {} - IP: {}",
                requestId, method, requestURI,
                queryString != null ? "?" + queryString : "",
                userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 50)) : "unknown",
                remoteAddr);

        try {
            // 실제 요청 처리
            chain.doFilter(request, response);
        } finally {
            // 응답 정보 로깅
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();

            log.info("[{}] {} {} - Status: {} - Duration: {}ms",
                    requestId, method, requestURI, status, duration);
        }
    }

    private boolean isStaticResource(String requestURI) {
        return requestURI.startsWith("/static/") ||
               requestURI.startsWith("/css/") ||
               requestURI.startsWith("/js/") ||
               requestURI.startsWith("/images/") ||
               requestURI.startsWith("/favicon.") ||
               requestURI.endsWith(".css") ||
               requestURI.endsWith(".js") ||
               requestURI.endsWith(".png") ||
               requestURI.endsWith(".jpg") ||
               requestURI.endsWith(".gif") ||
               requestURI.endsWith(".ico");
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}