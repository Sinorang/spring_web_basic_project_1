package com.elice.boardproject.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

/**
 * 애플리케이션에서 발생할 수 있는 모든 에러 코드를 정의
 * 메시지에 파라미터를 포함할 수 있도록 MessageFormat을 사용
 */
@Getter
public enum ErrorCode {
    
    // 공통 에러 (1000번대)
    INVALID_INPUT_VALUE(1000, HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(1001, HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(1002, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(1003, HttpStatus.BAD_REQUEST, "잘못된 타입의 값입니다."),
    HANDLE_ACCESS_DENIED(1004, HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),
    
    // 인증/인가 에러 (2000번대)
    UNAUTHORIZED(2000, HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(2001, HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INVALID_TOKEN(2002, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(2003, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_CREDENTIALS(2004, HttpStatus.UNAUTHORIZED, "잘못된 인증 정보입니다."),
    
    // 사용자 관련 에러 (3000번대)
    USER_NOT_FOUND(3000, HttpStatus.NOT_FOUND, "ID: {0}인 사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(3001, HttpStatus.CONFLICT, "ID: {0}인 사용자가 이미 존재합니다."),
    INVALID_PASSWORD(3002, HttpStatus.BAD_REQUEST, "잘못된 비밀번호입니다."),
    PASSWORD_MISMATCH(3003, HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    EMAIL_ALREADY_EXISTS(3004, HttpStatus.CONFLICT, "이메일 {0}은(는) 이미 사용 중입니다."),
    NICKNAME_ALREADY_EXISTS(3005, HttpStatus.CONFLICT, "닉네임 {0}은(는) 이미 사용 중입니다."),
    OAUTH_USER_UPDATE_RESTRICTED(3006, HttpStatus.BAD_REQUEST, "OAuth 사용자는 이 정보를 수정할 수 없습니다."),
    
    // 게시판 관련 에러 (4000번대)
    BOARD_NOT_FOUND(4000, HttpStatus.NOT_FOUND, "게시판 ID: {0}을(를) 찾을 수 없습니다."),
    BOARD_ACCESS_DENIED(4001, HttpStatus.FORBIDDEN, "게시판에 접근할 권한이 없습니다."),
    BOARD_ALREADY_EXISTS(4002, HttpStatus.CONFLICT, "게시판명 {0}은(는) 이미 존재합니다."),
    
    // 게시글 관련 에러 (5000번대)
    POST_NOT_FOUND(5000, HttpStatus.NOT_FOUND, "게시글 ID: {0}을(를) 찾을 수 없습니다."),
    POST_ACCESS_DENIED(5001, HttpStatus.FORBIDDEN, "게시글에 접근할 권한이 없습니다."),
    POST_UPDATE_DENIED(5002, HttpStatus.FORBIDDEN, "게시글을 수정할 권한이 없습니다."),
    POST_DELETE_DENIED(5003, HttpStatus.FORBIDDEN, "게시글을 삭제할 권한이 없습니다."),
    
    // 댓글 관련 에러 (6000번대)
    COMMENT_NOT_FOUND(6000, HttpStatus.NOT_FOUND, "댓글 ID: {0}을(를) 찾을 수 없습니다."),
    COMMENT_ACCESS_DENIED(6001, HttpStatus.FORBIDDEN, "댓글에 접근할 권한이 없습니다."),
    COMMENT_UPDATE_DENIED(6002, HttpStatus.FORBIDDEN, "댓글을 수정할 권한이 없습니다."),
    COMMENT_DELETE_DENIED(6003, HttpStatus.FORBIDDEN, "댓글을 삭제할 권한이 없습니다."),
    
    // 플레이리스트 관련 에러 (7000번대)
    PLAYLIST_NOT_FOUND(7000, HttpStatus.NOT_FOUND, "플레이리스트 ID: {0}을(를) 찾을 수 없습니다."),
    PLAYLIST_ACCESS_DENIED(7001, HttpStatus.FORBIDDEN, "플레이리스트에 접근할 권한이 없습니다."),
    PLAYLIST_UPDATE_DENIED(7002, HttpStatus.FORBIDDEN, "플레이리스트를 수정할 권한이 없습니다."),
    PLAYLIST_DELETE_DENIED(7003, HttpStatus.FORBIDDEN, "플레이리스트를 삭제할 권한이 없습니다."),
    INVALID_YOUTUBE_URL(7004, HttpStatus.BAD_REQUEST, "유효하지 않은 YouTube URL입니다: {0}"),
    INVALID_PLAYLIST_URL(7005, HttpStatus.BAD_REQUEST, "유효하지 않은 플레이리스트 URL입니다: {0}"),
    YOUTUBE_API_ERROR(7006, HttpStatus.SERVICE_UNAVAILABLE, "YouTube API 호출 중 오류가 발생했습니다."),
    PLAYLIST_FETCH_ERROR(7007, HttpStatus.SERVICE_UNAVAILABLE, "플레이리스트 정보를 가져오는 중 오류가 발생했습니다."),
    PRIVATE_PLAYLIST_ERROR(7008, HttpStatus.FORBIDDEN, "비공개 플레이리스트는 가져올 수 없습니다."),
    
    // OAuth 관련 에러 (8000번대)
    OAUTH_AUTHENTICATION_FAILED(8000, HttpStatus.UNAUTHORIZED, "OAuth 인증에 실패했습니다."),
    OAUTH_USER_INFO_ERROR(8001, HttpStatus.SERVICE_UNAVAILABLE, "OAuth 사용자 정보를 가져오는 중 오류가 발생했습니다."),
    OAUTH_PROVIDER_ERROR(8002, HttpStatus.SERVICE_UNAVAILABLE, "OAuth 제공자 서비스 오류가 발생했습니다."),
    
    // 파일 업로드 관련 에러 (9000번대)
    FILE_UPLOAD_ERROR(9000, HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다."),
    INVALID_FILE_TYPE(9001, HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다: {0}"),
    FILE_SIZE_EXCEEDED(9002, HttpStatus.BAD_REQUEST, "파일 크기가 제한을 초과했습니다. 최대 크기: {0}"),
    FILE_NOT_FOUND(9003, HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다: {0}"),
    
    // 관리자 관련 에러 (10000번대)
    ADMIN_ROLE_NOT_FOUND(10000, HttpStatus.NOT_FOUND, "관리자 역할 {0}을(를) 찾을 수 없습니다."),
    ADMIN_ROLE_ALREADY_EXISTS(10001, HttpStatus.CONFLICT, "관리자 역할 {0}은(는) 이미 존재합니다."),
    PERMISSION_NOT_FOUND(10002, HttpStatus.NOT_FOUND, "권한 {0}을(를) 찾을 수 없습니다."),
    PERMISSION_ALREADY_EXISTS(10003, HttpStatus.CONFLICT, "권한 {0}은(는) 이미 존재합니다."),
    ADMIN_ACCESS_DENIED(10004, HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다."),
    USER_NOT_ADMIN(10005, HttpStatus.FORBIDDEN, "사용자 {0}은(는) 관리자가 아닙니다.");
    
    private final int code;
    private final HttpStatus httpStatus;
    private final String messageTemplate;
    
    ErrorCode(int code, HttpStatus httpStatus, String messageTemplate) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.messageTemplate = messageTemplate;
    }
    
    /**
     * 기본 메시지를 반환합니다.
     */
    public String getMessage() {
        return messageTemplate;
    }
    
    /**
     * 파라미터를 포함한 메시지를 반환합니다.
     * @param args 메시지에 포함할 파라미터들
     * @return 포맷된 메시지
     */
    public String getMessage(Object... args) {
        return MessageFormat.format(messageTemplate, args);
    }
} 