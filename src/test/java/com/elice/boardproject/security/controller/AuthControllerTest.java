package com.elice.boardproject.security.controller;

import com.elice.boardproject.security.entity.RefreshToken;
import com.elice.boardproject.security.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String testUserId;
    private String testRefreshToken;
    private RefreshToken testRefreshTokenEntity;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        testUserId = "testuser";
        testRefreshToken = "test-refresh-token";
        testRefreshTokenEntity = RefreshToken.builder()
                .id(1L)
                .userId(testUserId)
                .token(testRefreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("유효한 RefreshToken으로 새로운 AccessToken을 발급받을 수 있다")
    void refreshToken_Success() throws Exception {
        // given
        when(refreshTokenService.validateRefreshToken(testRefreshToken)).thenReturn(true);
        when(refreshTokenService.getUserIdFromToken(testRefreshToken)).thenReturn(Optional.of(testUserId));

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", testRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token이 성공적으로 갱신되었습니다."))
                .andExpect(jsonPath("$.accessToken").exists());

        verify(refreshTokenService).validateRefreshToken(testRefreshToken);
        verify(refreshTokenService).getUserIdFromToken(testRefreshToken);
    }

    @Test
    @DisplayName("RefreshToken이 없으면 에러를 반환한다")
    void refreshToken_NoRefreshToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("RefreshToken이 없습니다."));

        verify(refreshTokenService, never()).validateRefreshToken(any());
    }

    @Test
    @DisplayName("유효하지 않은 RefreshToken이면 에러를 반환한다")
    void refreshToken_InvalidRefreshToken() throws Exception {
        // given
        when(refreshTokenService.validateRefreshToken(testRefreshToken)).thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", testRefreshToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 RefreshToken입니다."));

        verify(refreshTokenService).validateRefreshToken(testRefreshToken);
        verify(refreshTokenService, never()).getUserIdFromToken(any());
    }

    @Test
    @DisplayName("RefreshToken에서 사용자 정보를 찾을 수 없으면 에러를 반환한다")
    void refreshToken_UserNotFound() throws Exception {
        // given
        when(refreshTokenService.validateRefreshToken(testRefreshToken)).thenReturn(true);
        when(refreshTokenService.getUserIdFromToken(testRefreshToken)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", testRefreshToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("RefreshToken에서 사용자 정보를 찾을 수 없습니다."));

        verify(refreshTokenService).validateRefreshToken(testRefreshToken);
        verify(refreshTokenService).getUserIdFromToken(testRefreshToken);
    }

    @Test
    @DisplayName("로그아웃 시 RefreshToken을 삭제하고 쿠키를 제거한다")
    void logout_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("jwt_token", "valid-access-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다."));
    }

    @Test
    @DisplayName("로그아웃 시 AccessToken이 없어도 성공한다")
    void logout_NoAccessToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다."));

        verify(refreshTokenService, never()).deleteByUserId(any());
    }
} 