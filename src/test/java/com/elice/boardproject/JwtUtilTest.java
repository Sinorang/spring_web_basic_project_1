package com.elice.boardproject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.elice.boardproject.security.JwtUtil;

class JwtUtilTest {

    @Test
    void 토큰_생성_테스트() {
        String username = "testuser";
        String token = JwtUtil.generateToken(username);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 3개의 부분으로 구성
    }

    @Test
    void 토큰에서_사용자명_추출_테스트() {
        String username = "testuser";
        String token = JwtUtil.generateToken(username);
        
        String extractedUsername = JwtUtil.getUsernameFromToken(token);
        
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void 유효한_토큰_검증_테스트() {
        String username = "testuser";
        String token = JwtUtil.generateToken(username);
        
        boolean isValid = JwtUtil.validateToken(token);
        
        assertThat(isValid).isTrue();
    }

    @Test
    void 무효한_토큰_검증_테스트() {
        String invalidToken = "invalid.token.here";
        
        boolean isValid = JwtUtil.validateToken(invalidToken);
        
        assertThat(isValid).isFalse();
    }

    @Test
    void null_토큰_검증_테스트() {
        boolean isValid = JwtUtil.validateToken(null);
        
        assertThat(isValid).isFalse();
    }

    @Test
    void 빈_토큰_검증_테스트() {
        boolean isValid = JwtUtil.validateToken("");
        
        assertThat(isValid).isFalse();
    }

    @Test
    void 서로_다른_사용자명으로_생성된_토큰_구분_테스트() {
        String username1 = "user1";
        String username2 = "user2";
        
        String token1 = JwtUtil.generateToken(username1);
        String token2 = JwtUtil.generateToken(username2);
        
        String extractedUsername1 = JwtUtil.getUsernameFromToken(token1);
        String extractedUsername2 = JwtUtil.getUsernameFromToken(token2);
        
        assertThat(extractedUsername1).isEqualTo(username1);
        assertThat(extractedUsername2).isEqualTo(username2);
        assertThat(extractedUsername1).isNotEqualTo(extractedUsername2);
    }
} 