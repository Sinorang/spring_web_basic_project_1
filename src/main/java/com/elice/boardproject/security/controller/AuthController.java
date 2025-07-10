package com.elice.boardproject.security.controller;

import com.elice.boardproject.security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * AuthController는 JWT 기반 인증/토큰 관리 전용 REST API를 제공합니다.
 *
 * - /api/auth/refresh : refreshToken으로 accessToken 재발급
 * - /api/auth/logout  : refreshToken 및 accessToken 폐기(쿠키/DB)
 *
 * 이 API들은 현재 화면(Web)에서는 직접 사용하지 않지만,
 * 추후 SPA(React, Vue 등) 프론트엔드, 모바일 앱, 외부 서비스 연동,
 * refreshToken 기반 보안 고도화, API 일관성 확보 등을 위해 미리 구현되었습니다.
 *
 * 기존 UserController의 화면 기반 로그아웃과 병행 가능하며,
 * 점진적으로 API 기반 인증 체계로 전환할 때 활용할 수 있습니다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final RefreshTokenService refreshTokenService;

    /**
     * RefreshToken을 사용하여 새로운 AccessToken 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        try {
            // 쿠키에서 refreshToken 추출
            String refreshToken = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refresh_token".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }

            if (refreshToken == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "RefreshToken이 없습니다."
                ));
            }

            // RefreshToken 검증
            if (!refreshTokenService.validateRefreshToken(refreshToken)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "유효하지 않은 RefreshToken입니다."
                ));
            }

            // 사용자 ID 추출
            String userId = refreshTokenService.getUserIdFromToken(refreshToken)
                    .orElse(null);
            
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "RefreshToken에서 사용자 정보를 찾을 수 없습니다."
                ));
            }

            // 새로운 AccessToken 생성
            String newAccessToken = com.elice.boardproject.security.JwtUtil.generateAccessToken(userId);

            // 새로운 AccessToken을 쿠키에 설정
            Cookie accessTokenCookie = new Cookie("jwt_token", newAccessToken);
            accessTokenCookie.setHttpOnly(false); // JavaScript에서 읽을 수 있도록 false로 설정
            accessTokenCookie.setSecure(request.isSecure());
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(1800); // 30분
            response.addCookie(accessTokenCookie);

            log.info("Token 갱신 성공 - 사용자: {}", userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token이 성공적으로 갱신되었습니다.",
                "accessToken", newAccessToken
            ));

        } catch (Exception e) {
            log.error("Token 갱신 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Token 갱신 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 로그아웃 - RefreshToken 삭제
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        try {
            // 현재 사용자 ID 추출
            String userId = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt_token".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        if (com.elice.boardproject.security.JwtUtil.validateToken(token)) {
                            userId = com.elice.boardproject.security.JwtUtil.getUsernameFromToken(token);
                        }
                        break;
                    }
                }
            }

            // RefreshToken 삭제
            if (userId != null) {
                refreshTokenService.deleteByUserId(userId);
            }

            // 쿠키 삭제
            Cookie accessTokenCookie = new Cookie("jwt_token", null);
            accessTokenCookie.setHttpOnly(false);
            accessTokenCookie.setMaxAge(0);
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refresh_token", null);
            refreshTokenCookie.setHttpOnly(false);
            refreshTokenCookie.setMaxAge(0);
            refreshTokenCookie.setPath("/");
            response.addCookie(refreshTokenCookie);

            log.info("로그아웃 성공 - 사용자: {}", userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그아웃되었습니다."
            ));

        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "로그아웃 중 오류가 발생했습니다."
            ));
        }
    }
} 