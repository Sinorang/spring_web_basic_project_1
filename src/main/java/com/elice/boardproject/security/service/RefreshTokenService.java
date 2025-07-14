package com.elice.boardproject.security.service;

import com.elice.boardproject.security.entity.RefreshToken;
import com.elice.boardproject.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    
    /**
     * 사용자 ID로 새로운 RefreshToken 생성
     */
    @Transactional
    public RefreshToken createRefreshToken(String userId, long expirationDays) {
        // 기존 토큰이 있으면 삭제
        refreshTokenRepository.deleteByUserId(userId);
        
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(expirationDays);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(expiresAt)
                .build();
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    /**
     * 토큰으로 RefreshToken 조회
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    /**
     * 사용자 ID로 RefreshToken 조회
     */
    public Optional<RefreshToken> findByUserId(String userId) {
        return refreshTokenRepository.findByUserId(userId);
    }
    
    /**
     * RefreshToken 검증
     */
    public boolean validateRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = findByToken(token);
        if (refreshTokenOpt.isEmpty()) {
            return false;
        }
        
        RefreshToken refreshToken = refreshTokenOpt.get();
        return !refreshToken.getExpiresAt().isBefore(LocalDateTime.now());
    }
    
    /**
     * 사용자 ID로 RefreshToken 삭제
     */
    @Transactional
    public void deleteByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
    
    /**
     * 만료된 토큰들 삭제
     */
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
    
    /**
     * 토큰으로 사용자 ID 조회
     */
    public Optional<String> getUserIdFromToken(String token) {
        return findByToken(token)
                .filter(rt -> !rt.getExpiresAt().isBefore(LocalDateTime.now()))
                .map(RefreshToken::getUserId);
    }
} 