package io.goorm.board.auth;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
/* 로그인 실패 대응 로직 */
public class AuthFailureHandler implements AuthenticationFailureHandler {

  private final MessageSource messageSource;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request,
                                      HttpServletResponse response, AuthenticationException exception)
          throws IOException, ServletException {

    String code = "login.message.notfound";

    if (exception instanceof AuthenticationServiceException) {
      code = "login.message.notfound"; // ex: "존재하지 않는 사용자입니다."
    } else if (exception instanceof BadCredentialsException) {
//      code = "login.message.invalid";  // ex: "아이디 또는 비밀번호가 틀립니다."
      code = "login.message.notfound";
    } else if (exception instanceof LockedException) {
//      code = "login.message.locked";
      code = "login.message.notfound";
    } else if (exception instanceof DisabledException) {
//      code = "login.message.disabled";
      code = "login.message.notfound";
    } else if (exception instanceof AccountExpiredException) {
//      code = "login.message.accountExpired";
      code = "login.message.notfound";
    } else if (exception instanceof CredentialsExpiredException) {
//      code = "login.message.credentialsExpired";
      code = "login.message.notfound";
    }

    // 메시지 리소스에서 가져오기
    String msg = messageSource.getMessage(code, null,
            "로그인에 실패했습니다.", LocaleContextHolder.getLocale());

    String encodedMsg = URLEncoder.encode(msg, StandardCharsets.UTF_8.toString());
    String loginId = request.getParameter("email");
    String encodedLoginId = URLEncoder.encode(loginId, StandardCharsets.UTF_8.toString());

    response.sendRedirect(request.getContextPath() + "/auth/login?error=true&message=" + encodedMsg + "&last_login_id=" + encodedLoginId);

  }
}