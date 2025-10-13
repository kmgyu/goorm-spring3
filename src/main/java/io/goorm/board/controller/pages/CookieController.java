package io.goorm.board.controller.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages/cookie")
@RequiredArgsConstructor
public class CookieController {

    @GetMapping("/list")
    public String listPage() {
        return "pages/cookie/list";
    }

    @GetMapping("/write")
    public String writePage() {
        return "pages/cookie/write";
    }
}