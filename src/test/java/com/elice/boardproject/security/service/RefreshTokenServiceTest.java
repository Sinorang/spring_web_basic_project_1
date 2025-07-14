package com.elice.boardproject.security.service;

import com.elice.boardproject.security.entity.RefreshToken;
import com.elice.boardproject.security.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private String testUserId;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUserId = "testuser";
        testRefreshToken = RefreshToken.builder()
                .id(1L)
                .userId(testUserId)
                .token("test-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("새로운 RefreshToken을 생성할 수 있다")
    void createRefreshToken_Success() {
        // given
        doNothing().when(refreshTokenRepository).deleteByUserId(testUserId);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        // when
        RefreshToken result = refreshTokenService.createRefreshToken(testUserId, 7);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(testUserId);
        assertThat(result.getToken()).isEqualTo("test-refresh-token");
        verify(refreshTokenRepository).deleteByUserId(testUserId);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("토큰으로 RefreshToken을 조회할 수 있다")
    void findByToken_Success() {
        // given
        String token = "test-refresh-token";
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(testRefreshToken));

        // when
        Optional<RefreshToken> result = refreshTokenService.findByToken(token);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(token);
        verify(refreshTokenRepository).findByToken(token);
    }

    @Test
    @DisplayName("존재하지 않는 토큰으로 조회하면 빈 Optional을 반환한다")
    void findByToken_NotFound() {
        // given
        String token = "non-existent-token";
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // when
        Optional<RefreshToken> result = refreshTokenService.findByToken(token);

        // then
        assertThat(result).isEmpty();
        verify(refreshTokenRepository).findByToken(token);
    }

    @Test
    @DisplayName("사용자 ID로 RefreshToken을 조회할 수 있다")
    void findByUserId_Success() {
        // given
        when(refreshTokenRepository.findByUserId(testUserId)).thenReturn(Optional.of(testRefreshToken));

        // when
        Optional<RefreshToken> result = refreshTokenService.findByUserId(testUserId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(testUserId);
        verify(refreshTokenRepository).findByUserId(testUserId);
    }

    @Test
    @DisplayName("유효한 RefreshToken을 검증할 수 있다")
    void validateRefreshToken_Valid() {
        // given
        String token = "valid-token";
        RefreshToken validToken = RefreshToken.builder()
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(validToken));

        // when
        boolean result = refreshTokenService.validateRefreshToken(token);

        // then
        assertThat(result).isTrue();
        verify(refreshTokenRepository).findByToken(token);
    }

    @Test
    @DisplayName("만료된 RefreshToken은 유효하지 않다")
    void validateRefreshToken_Expired() {
        // given
        String token = "expired-token";
        RefreshToken expiredToken = RefreshToken.builder()
                .token(token)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(expiredToken));

        // when
        boolean result = refreshTokenService.validateRefreshToken(token);

        // then
        assertThat(result).isFalse();
        verify(refreshTokenRepository).findByToken(token);
    }

    @Test
    @DisplayName("존재하지 않는 RefreshToken은 유효하지 않다")
    void validateRefreshToken_NotFound() {
        // given
        String token = "non-existent-token";
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // when
        boolean result = refreshTokenService.validateRefreshToken(token);

        // then
        assertThat(result).isFalse();
        verify(refreshTokenRepository).findByToken(token);
    }

    @Test
    @DisplayName("사용자 ID로 RefreshToken을 삭제할 수 있다")
    void deleteByUserId_Success() {
        // given
        doNothing().when(refreshTokenRepository).deleteByUserId(testUserId);

        // when
        refreshTokenService.deleteByUserId(testUserId);

        // then
        verify(refreshTokenRepository).deleteByUserId(testUserId);
    }

    @Test
    @DisplayName("만료된 토큰들을 삭제할 수 있다")
    void deleteExpiredTokens_Success() {
        // given
        doNothing().when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));

        // when
        refreshTokenService.deleteExpiredTokens();

        // then
        verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("유효한 토큰에서 사용자 ID를 추출할 수 있다")
    void getUserIdFromToken_Valid() {
        // given
        String token = "valid-token";
        RefreshToken validToken = RefreshToken.builder()
                .userId(testUserId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(validToken));

        // when
        Optional<String> result = refreshTokenService.getUserIdFromToken(token);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUserId);
        verify(refreshTokenRepository).findByToken(token);
    }

    @Test
    @DisplayName("만료된 토큰에서는 사용자 ID를 추출할 수 없다")
    void getUserIdFromToken_Expired() {
        // given
        String token = "expired-token";
        RefreshToken expiredToken = RefreshToken.builder()
                .userId(testUserId)
                .token(token)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(expiredToken));

        // when
        Optional<String> result = refreshTokenService.getUserIdFromToken(token);

        // then
        assertThat(result).isEmpty();
        verify(refreshTokenRepository).findByToken(token);
    }
} 