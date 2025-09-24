package io.goorm.board.controller;

import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
//@Controller  // 임시 비활성화 - PostService 없음
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    // 메인 페이지
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // 게시글 목록 (기본)
    @GetMapping("/posts")
    public String list(Model model) {
        log.debug("게시글 목록 요청 - /posts");
        List<Post> posts = postService.findAll();
        log.debug("게시글 {} 개 조회 완료", posts.size());

        model.addAttribute("posts", posts);
        return "post/list";
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{seq}")
    public String show(@PathVariable Long seq, Model model) {
        Post post = postService.findBySeq(seq);
        model.addAttribute("post", post);
        return "post/show";
    }

    // 게시글 작성 폼
    @GetMapping("/posts/new")
    public String createForm(Model model) {
        model.addAttribute("post", new Post());
        return "post/form";
    }

    // 게시글 저장 → 목록으로
    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute Post post,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal User user,
                        RedirectAttributes redirectAttributes,
                        HttpServletRequest request) {

        // 검증 오류가 있으면 폼으로 다시 이동
        if (bindingResult.hasErrors()) {
            return "post/form";
        }

        // 작성자 설정
        post.setAuthor(user);

        // 검증 통과 시에만 저장
        postService.save(post);

        String message = messageSource.getMessage("flash.post.created", null, localeResolver.resolveLocale(request));
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/posts";
    }

    // 게시글 수정 폼 (권한 체크는 서비스에서)
    @GetMapping("/posts/{seq}/edit")
    public String editForm(@PathVariable Long seq, Model model) {
        Post post = postService.findForEdit(seq); // 서비스에서 권한 체크
        model.addAttribute("post", post);
        return "post/form";
    }

    // 게시글 수정 → 상세보기로
    @PostMapping("/posts/{seq}")
    public String update(@PathVariable Long seq,
                        @Valid @ModelAttribute Post post,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        HttpServletRequest request) {

        // 검증 오류가 있으면 폼으로 다시 이동
        if (bindingResult.hasErrors()) {
            post.setSeq(seq); // seq 값 설정 (수정 폼에서 필요)
            return "post/form";
        }

        // 서비스에서 @PreAuthorize로 권한 체크
        postService.update(seq, post);

        String message = messageSource.getMessage("flash.post.updated", null, localeResolver.resolveLocale(request));
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/posts/" + seq;
    }

    // 게시글 삭제 → 목록으로
    @PostMapping("/posts/{seq}/delete")
    public String delete(@PathVariable Long seq, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        // 서비스에서 @PreAuthorize로 권한 체크
        postService.delete(seq);

        String message = messageSource.getMessage("flash.post.deleted", null, localeResolver.resolveLocale(request));
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/posts";
    }


}
