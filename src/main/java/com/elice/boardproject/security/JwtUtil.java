package com.elice.boardproject.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkeymysecretkey"; // 256bit 이상 권장
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7일
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // AccessToken 생성 (기존 메서드와 호환성을 위해 유지)
    public static String generateToken(String username) {
        return generateAccessToken(username);
    }

    // AccessToken 생성
    public static String generateAccessToken(String username) {
        logger.debug("AccessToken 생성 시작 - 사용자: {}", username);
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .claim("type", "access")
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
        logger.debug("AccessToken 생성 완료 - 사용자: {}", username);
        return token;
    }

    // RefreshToken 생성
    public static String generateRefreshToken(String username) {
        logger.debug("RefreshToken 생성 시작 - 사용자: {}", username);
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME))
                .claim("type", "refresh")
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
        logger.debug("RefreshToken 생성 완료 - 사용자: {}", username);
        return token;
    }

    // 토큰에서 사용자명 추출
    public static String getUsernameFromToken(String token) {
        logger.debug("JWT 토큰에서 사용자명 추출 시작");
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String username = claims.getSubject();
            logger.debug("JWT 토큰에서 사용자명 추출 완료 - 사용자: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("JWT 토큰에서 사용자명 추출 실패: {}", e.getMessage());
            throw e;
        }
    }

    // 토큰 유효성 검증
    public static boolean validateToken(String token) {
        logger.debug("JWT 토큰 유효성 검증 시작");
        try {
            Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
            logger.debug("JWT 토큰 유효성 검증 성공");
            return true;
        } catch (Exception e) {
            logger.warn("JWT 토큰 유효성 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // 토큰 타입 확인
    public static String getTokenType(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("type", String.class);
        } catch (Exception e) {
            logger.error("토큰 타입 확인 실패: {}", e.getMessage());
            return null;
        }
    }

    // AccessToken인지 확인
    public static boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    // RefreshToken인지 확인
    public static boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }
} 