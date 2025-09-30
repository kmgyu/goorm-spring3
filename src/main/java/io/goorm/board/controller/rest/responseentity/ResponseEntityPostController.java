package io.goorm.board.controller.rest.responseentity;

import io.goorm.board.dto.ApiResponse;
import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/responseentity/post")
public class ResponseEntityPostController {
  private final PostService postService;
  // todo : i18n message refactor
  private final MessageSource messageSource;
  private final LocaleResolver localeResolver;

  @GetMapping
  public ResponseEntity<ApiResponse<List<Post>>> getAllPosts() {
    List<Post> posts = postService.findAll();
    ApiResponse<List<Post>> response = ApiResponse.success("게시글 목록 조회 성공", posts);
    return ResponseEntity.ok(response);
  }


  // 게시글 상세 조회
  @GetMapping("/{seq}")
  public ResponseEntity<ApiResponse<Post>> getPost(@PathVariable Long seq) {
    Post post = postService.findBySeq(seq);
    ApiResponse<Post> response = ApiResponse.success("게시글 조회 성공", post);

    return ResponseEntity.ok(response);
  }

  // 게시글 생성
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createPost(@Valid @RequestBody Post post,
                                        @AuthenticationPrincipal User user) {
    // 작성자 설정
    post.setAuthor(user);
    postService.save(post);

    ApiResponse<Void> response = ApiResponse.success("게시글 성공적으로 작성되었습니다.");
    return ResponseEntity.ok(response);
  }

  // 게시글 수정
  @PutMapping("/{seq}")
  public ResponseEntity<ApiResponse<Void>> updatePost(@PathVariable Long seq,
                                        @Valid @RequestBody Post post) {
    postService.update(seq, post);

    ApiResponse<Void> response = ApiResponse.success("게시글 성공적으로 수정되었습니다.");
    return ResponseEntity.ok(response);
  }

  // 게시글 삭제
  @DeleteMapping("/{seq}")
  public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long seq) {
    postService.delete(seq);

    ApiResponse<Void> response = ApiResponse.success("게시글 성공적으로 삭제되었습니다.");
    return ResponseEntity.ok(response);
  }

}
