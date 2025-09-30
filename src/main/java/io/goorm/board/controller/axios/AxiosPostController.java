package io.goorm.board.controller.axios;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import io.goorm.board.entity.User;

@Slf4j
@Controller
@RequestMapping("/axios")
@RequiredArgsConstructor
public class AxiosPostController {

    @GetMapping("/posts")
    public String posts(@AuthenticationPrincipal User user, Model model) {
        log.debug("Axios 게시글 목록 페이지 요청 - 사용자: {}", user.getEmail());
        model.addAttribute("user", user);
        return "axios/post/list";
    }

    @GetMapping("/posts/new")
    public String newPost(@AuthenticationPrincipal User user, Model model) {
        log.debug("Axios 게시글 작성 페이지 요청 - 사용자: {}", user.getEmail());
        model.addAttribute("user", user);
        model.addAttribute("mode", "new");
        return "axios/post/form";
    }

    @GetMapping("/posts/{id}")
    public String viewPost(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        log.debug("Axios 게시글 상세 페이지 요청 - 게시글 ID: {}, 사용자: {}", id, user.getEmail());
        model.addAttribute("user", user);
        model.addAttribute("postId", id);
        return "axios/post/view";
    }

    @GetMapping("/posts/{id}/edit")
    public String editPost(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        log.debug("Axios 게시글 수정 페이지 요청 - 게시글 ID: {}, 사용자: {}", id, user.getEmail());
        model.addAttribute("user", user);
        model.addAttribute("postId", id);
        model.addAttribute("mode", "edit");
        return "axios/post/form";
    }
}