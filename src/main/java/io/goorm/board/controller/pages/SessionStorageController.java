package io.goorm.board.controller.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages/sessionStorage")
@RequiredArgsConstructor
public class SessionStorageController {

    @GetMapping("/list")
    public String listPage() {
        return "pages/sessionStorage/list";
    }

    @GetMapping("/write")
    public String writePage() {
        return "pages/sessionStorage/write";
    }
}