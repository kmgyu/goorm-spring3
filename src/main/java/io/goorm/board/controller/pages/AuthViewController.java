package io.goorm.board.controller.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages/auth")
@RequiredArgsConstructor
public class AuthViewController {

    @GetMapping("/login")
    public String loginPage() {
        return "pages/auth/login";
    }
}