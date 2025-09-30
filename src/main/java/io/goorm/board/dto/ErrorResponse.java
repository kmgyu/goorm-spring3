package io.goorm.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

  private boolean success;
  private ErrorDetail error;
  private LocalDateTime timestamp;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ErrorDetail {
    private String code;
    private String message;
    private int status;
    private Map<String, String> fieldErrors;
  }

  public static ErrorResponse of(String code, String message, int status) {
    return ErrorResponse.builder()
            .success(false)
            .error(ErrorDetail.builder()
                    .code(code)
                    .message(message)
                    .status(status)
                    .build())
            .timestamp(LocalDateTime.now())
            .build();
  }

  public static ErrorResponse of(String code, String message, int status, Map<String, String> fieldErrors) {
    return ErrorResponse.builder()
            .success(false)
            .error(ErrorDetail.builder()
                    .code(code)
                    .message(message)
                    .status(status)
                    .fieldErrors(fieldErrors)
                    .build())
            .timestamp(LocalDateTime.now())
            .build();
  }
}