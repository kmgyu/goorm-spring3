package io.goorm.board.controller.fetch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/fetch/auth")
@RequiredArgsConstructor
public class FetchAuthController {

  @GetMapping("/login")
  public String loginPage() {
    return "fetch/auth/login";
  }
}