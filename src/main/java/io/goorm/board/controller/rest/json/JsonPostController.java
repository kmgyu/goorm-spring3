package io.goorm.board.controller.rest.json;

import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/json/posts")
@RequiredArgsConstructor
public class JsonPostController {

  private final PostService postService;

  // 게시글 목록 조회
  @GetMapping
  public List<Post> getAllPosts() {
    return postService.findAll();
  }

  // 게시글 상세 조회
  @GetMapping("/{seq}")
  public Post getPost(@PathVariable Long seq) {
    return postService.findBySeq(seq);
  }

  // 게시글 생성
  @PostMapping
  public Map<String, Object> createPost(@Valid @RequestBody Post post,
                                        @AuthenticationPrincipal User user) {
    // 작성자 설정
    post.setAuthor(user);
    postService.save(post);

    // 성공 응답 생성
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "게시글이 성공적으로 작성되었습니다.");
    return response;
  }

  // 게시글 수정
  @PutMapping("/{seq}")
  public Map<String, Object> updatePost(@PathVariable Long seq,
                                        @Valid @RequestBody Post post) {
    postService.update(seq, post);

    // 성공 응답 생성
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "게시글이 성공적으로 수정되었습니다.");
    response.put("updatedSeq", seq);
    return response;
  }

  // 게시글 삭제
  @DeleteMapping("/{seq}")
  public Map<String, Object> deletePost(@PathVariable Long seq) {
    postService.delete(seq);

    // 삭제 성공 응답 생성
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "게시글이 성공적으로 삭제되었습니다.");
    response.put("deletedSeq", seq);
    return response;
  }
}