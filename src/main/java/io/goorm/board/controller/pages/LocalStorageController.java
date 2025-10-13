package io.goorm.board.controller.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages/localStorage")
@RequiredArgsConstructor
public class LocalStorageController {

    @GetMapping("/list")
    public String listPage() {
        return "pages/localStorage/list";
    }

    @GetMapping("/write")
    public String writePage() {
        return "pages/localStorage/write";
    }
}