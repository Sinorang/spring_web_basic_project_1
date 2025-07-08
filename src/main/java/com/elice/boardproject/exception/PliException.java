package com.elice.boardproject.exception;

import lombok.Getter;

/**
 * 플레이리스트 애플리케이션의 커스텀 예외 클래스
 * 모든 비즈니스 로직 예외를 통합 관리
 */
@Getter
public class PliException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String message;
    
    public PliException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }
    
    public PliException(ErrorCode errorCode, Object... args) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage(args);
    }
    
    public PliException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
    
    public PliException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
    }
    
    public PliException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }
    
    public PliException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.getMessage(args), cause);
        this.errorCode = errorCode;
        this.message = errorCode.getMessage(args);
    }
} 