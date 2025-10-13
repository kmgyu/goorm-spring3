package io.goorm.board.controller.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages/jwt")
@RequiredArgsConstructor
public class JwtPostViewController {

    @GetMapping("/list")
    public String listPage() {
        return "jwt/posts/list";
    }

    @GetMapping("/write")
    public String writePage() {
        return "jwt/posts/write";
    }
}