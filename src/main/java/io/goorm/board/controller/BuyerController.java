package io.goorm.board.controller;

import io.goorm.board.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/buyer")
public class BuyerController {

  @GetMapping("/dashboard")
  public String dashboard(Model model, @AuthenticationPrincipal User user) {

    model.addAttribute("user", user);  // 회사 정보 포함해서 전달
    return "buyer/dashboard";
  }
  @GetMapping("/orders")
  public String orderList(Model model) {
    // 2단계에서 실제 로직 구현 예정 - 현재는 빈 리스트 화면만
    return "buyer/orders/list";
  }
}