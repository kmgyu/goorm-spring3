package io.goorm.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

  @GetMapping("/dashboard")    public String dashboard() {
    return "index";  // 기존 index.html이 관리자 대시보드로 사용
  }
}