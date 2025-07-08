package com.elice.boardproject.exception;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/**
 * 전역 예외 처리 시스템 테스트를 위한 컨트롤러
 * 실제 프로덕션에서는 제거해야 함
 */
@RestController
@RequestMapping("/api/test/exceptions")
@Profile({"test", "local"})
public class ExceptionTestController {
    /**
     * 커스텀 예외 테스트
     */
    @GetMapping("/custom")
    public String testCustomException() {
        throw new PliException(ErrorCode.USER_NOT_FOUND, "테스트용 커스텀 예외");
    }
    
    /**
     * 파라미터가 포함된 커스텀 예외 테스트
     */
    @GetMapping("/custom-with-params")
    public String testCustomExceptionWithParams() {
        throw new PliException(ErrorCode.USER_NOT_FOUND, "testuser123");
    }
    /**
     * 유효성 검증 실패 테스트
     */
    @PostMapping("/validation")
    public String testValidationException(@RequestParam(required = false) String requiredField) {
        ExceptionUtils.requireNonEmpty(requiredField, ErrorCode.INVALID_INPUT_VALUE, "필수 필드가 누락되었습니다.");
        return "성공";
    }
    /**
     * 조건 검증 실패 테스트
     */
    @GetMapping("/condition")
    public String testConditionException(@RequestParam(defaultValue = "false") boolean shouldFail) {
        ExceptionUtils.requireFalse(shouldFail, ErrorCode.INVALID_INPUT_VALUE, "조건 검증 실패");
        return "성공";
    }
    /**
     * IllegalArgumentException 테스트
     */
    @GetMapping("/illegal-argument")
    public String testIllegalArgumentException() {
        throw new IllegalArgumentException("잘못된 인수입니다.");
    }
    /**
     * 일반 예외 테스트
     */
    @GetMapping("/general")
    public String testGeneralException() {
        throw new RuntimeException("일반 런타임 예외입니다.");
    }
} 