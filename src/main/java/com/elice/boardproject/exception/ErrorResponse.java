package com.elice.boardproject.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 에러 응답을 위한 DTO 클래스
 * 일관된 에러 응답 형식을 제공
 */
@Getter
@Builder
public class ErrorResponse {
    
    private final int code;
    private final String message;
    private final HttpStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
    
    private final String path;
    private final List<FieldError> errors;
    
    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .status(errorCode.getHttpStatus())
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String path, List<FieldError> errors) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .status(errorCode.getHttpStatus())
                .timestamp(LocalDateTime.now())
                .path(path)
                .errors(errors)
                .build();
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String path, String customMessage) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(customMessage)
                .status(errorCode.getHttpStatus())
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
} 