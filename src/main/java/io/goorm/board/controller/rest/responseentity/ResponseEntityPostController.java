package io.goorm.board.controller.rest.responseentity;

import io.goorm.board.dto.ApiResponse;
import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/responseentity/post")
@Tag(name = "Post API (Session)", description = "세션 기반 게시글 관련 API")
@SecurityRequirement(name = "sessionAuth")
public class ResponseEntityPostController {

  private final PostService postService;
  private final MessageSource messageSource;

  @Operation(summary = "게시글 목록 조회", description = "모든 게시글 목록을 조회합니다.")
  @ApiResponses({
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                  content = @Content(schema = @Schema(implementation = Post.class)))
  })
  @GetMapping
  public ResponseEntity<ApiResponse<List<Post>>> getAllPosts() {
    List<Post> posts = postService.findAll();

    Locale locale = LocaleContextHolder.getLocale();
    String message = messageSource.getMessage("post.list.success", null, locale);

    ApiResponse<List<Post>> response = ApiResponse.success(message, posts);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
  @ApiResponses({
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                  content = @Content(schema = @Schema(implementation = Post.class))),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
  })
  @GetMapping("/{seq}")
  public ResponseEntity<ApiResponse<Post>> getPost(
          @Parameter(description = "게시글 번호", required = true) @PathVariable Long seq) {
    Post post = postService.findBySeq(seq); // 서비스에서 예외 처리

    Locale locale = LocaleContextHolder.getLocale();
    String message = messageSource.getMessage("post.detail.success", null, locale);

    ApiResponse<Post> response = ApiResponse.success(message, post);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다.")
  @ApiResponses({
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
  })
  @PostMapping
  public ResponseEntity<ApiResponse<Map<String, Object>>> createPost(
          @Parameter(description = "게시글 정보", required = true) @Valid @RequestBody Post post,
          @AuthenticationPrincipal User user) {
    post.setAuthor(user);
    postService.save(post);

    Locale locale = LocaleContextHolder.getLocale();
    String message = messageSource.getMessage("post.create.success", null, locale);

    Map<String, Object> data = Map.of("success", true);
    ApiResponse<Map<String, Object>> response = ApiResponse.success(message, data);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
  @ApiResponses({
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
  })
  @PutMapping("/{seq}")
  public ResponseEntity<ApiResponse<Map<String, Object>>> updatePost(
          @Parameter(description = "게시글 번호", required = true) @PathVariable Long seq,
          @Parameter(description = "수정할 게시글 정보", required = true) @Valid @RequestBody Post post) {
    postService.update(seq, post); // 서비스에서 예외 처리

    Locale locale = LocaleContextHolder.getLocale();
    String message = messageSource.getMessage("post.update.success", null, locale);

    Map<String, Object> data = Map.of("updatedSeq", seq);
    ApiResponse<Map<String, Object>> response = ApiResponse.success(message, data);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "게시글 삭제", description = "기존 게시글을 삭제합니다.")
  @ApiResponses({
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
  })
  @DeleteMapping("/{seq}")
  public ResponseEntity<ApiResponse<Map<String, Object>>> deletePost(
          @Parameter(description = "게시글 번호", required = true) @PathVariable Long seq) {
    postService.delete(seq); // 서비스에서 예외 처리

    Locale locale = LocaleContextHolder.getLocale();
    String message = messageSource.getMessage("post.delete.success", null, locale);

    Map<String, Object> data = Map.of("deletedSeq", seq);
    ApiResponse<Map<String, Object>> response = ApiResponse.success(message, data);
    return ResponseEntity.ok(response);
  }
}
