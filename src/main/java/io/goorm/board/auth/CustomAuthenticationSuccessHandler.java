package io.goorm.board.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
//@RequiredArgsConstructor
@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {

    String redirectUrl = determineTargetUrl(authentication);
    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }

  private String determineTargetUrl(Authentication authentication) {
    // ADMIN 권한이 있는지 확인
    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
      log.info("관리자 페이지 요청 {}", authentication.getName());
      return "/admin/dashboard";
    }
    // BUYER 권한이 있는지 확인
    else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_BUYER"))) {
      log.info("사용자 페이지 요청 {}", authentication.getName());
      return "/buyer/dashboard";
    }
    // 기본값 (혹시 모를 예외 상황)
    else {
      log.info("예외 발생, 사용자 : {}, 권한 : {}", authentication.getName(), authentication.getAuthorities());
      return "/";
    }
  }
}
