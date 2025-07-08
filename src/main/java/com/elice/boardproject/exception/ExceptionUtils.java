package com.elice.boardproject.exception;

import org.springframework.util.StringUtils;

/**
 * 예외 처리를 위한 유틸리티 클래스
 * 비즈니스 로직에서 쉽게 예외를 발생시킬 수 있도록 도움
 */
public class ExceptionUtils {
    
    /**
     * 객체가 null인지 확인하고 null이면 예외 발생
     */
    public static <T> T requireNonNull(T obj, ErrorCode errorCode) {
        if (obj == null) {
            throw new PliException(errorCode);
        }
        return obj;
    }
    
    /**
     * 객체가 null인지 확인하고 null이면 예외 발생 (커스텀 메시지)
     */
    public static <T> T requireNonNull(T obj, ErrorCode errorCode, String message) {
        if (obj == null) {
            throw new PliException(errorCode, message);
        }
        return obj;
    }
    
    /**
     * 객체가 null인지 확인하고 null이면 예외 발생 (파라미터 포함)
     */
    public static <T> T requireNonNull(T obj, ErrorCode errorCode, Object... args) {
        if (obj == null) {
            throw new PliException(errorCode, args);
        }
        return obj;
    }
    
    /**
     * 문자열이 비어있는지 확인하고 비어있으면 예외 발생
     */
    public static String requireNonEmpty(String str, ErrorCode errorCode) {
        if (!StringUtils.hasText(str)) {
            throw new PliException(errorCode);
        }
        return str;
    }
    
    /**
     * 문자열이 비어있는지 확인하고 비어있으면 예외 발생 (커스텀 메시지)
     */
    public static String requireNonEmpty(String str, ErrorCode errorCode, String message) {
        if (!StringUtils.hasText(str)) {
            throw new PliException(errorCode, message);
        }
        return str;
    }
    
    /**
     * 조건이 참인지 확인하고 거짓이면 예외 발생
     */
    public static void requireTrue(boolean condition, ErrorCode errorCode) {
        if (!condition) {
            throw new PliException(errorCode);
        }
    }
    
    /**
     * 조건이 참인지 확인하고 거짓이면 예외 발생 (커스텀 메시지)
     */
    public static void requireTrue(boolean condition, ErrorCode errorCode, String message) {
        if (!condition) {
            throw new PliException(errorCode, message);
        }
    }
    
    /**
     * 조건이 거짓인지 확인하고 참이면 예외 발생
     */
    public static void requireFalse(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new PliException(errorCode);
        }
    }
    
    /**
     * 조건이 거짓인지 확인하고 참이면 예외 발생 (커스텀 메시지)
     */
    public static void requireFalse(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            throw new PliException(errorCode, message);
        }
    }
    
    /**
     * 직접 예외 발생
     */
    public static void throwException(ErrorCode errorCode) {
        throw new PliException(errorCode);
    }
    
    /**
     * 직접 예외 발생 (파라미터 포함)
     */
    public static void throwException(ErrorCode errorCode, Object... args) {
        throw new PliException(errorCode, args);
    }
    
    /**
     * 직접 예외 발생 (원인과 함께)
     */
    public static void throwException(ErrorCode errorCode, Throwable cause) {
        throw new PliException(errorCode, cause);
    }
    
    /**
     * 직접 예외 발생 (파라미터와 원인)
     */
    public static void throwException(ErrorCode errorCode, Throwable cause, Object... args) {
        throw new PliException(errorCode, cause, args);
    }
    
    /**
     * 직접 예외 발생 (커스텀 메시지와 원인)
     */
    public static void throwException(ErrorCode errorCode, String message, Throwable cause) {
        throw new PliException(errorCode, message, cause);
    }
} 