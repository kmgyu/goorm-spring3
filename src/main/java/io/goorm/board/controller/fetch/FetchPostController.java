package io.goorm.board.controller.fetch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/fetch/posts")
@RequiredArgsConstructor
public class FetchPostController {

    @GetMapping
    public String listPage() {
        return "fetch/post/list";
    }

    @GetMapping("/new")
    public String createPage() {
        return "fetch/post/form";
    }

    @GetMapping("/{seq}")
    public String viewPage(@PathVariable Long seq, Model model) {
        model.addAttribute("seq", seq);
        return "fetch/post/view";
    }

    @GetMapping("/{seq}/edit")
    public String editPage(@PathVariable Long seq, Model model) {
        model.addAttribute("seq", seq);
        model.addAttribute("editMode", true);
        return "fetch/post/form";
    }
}