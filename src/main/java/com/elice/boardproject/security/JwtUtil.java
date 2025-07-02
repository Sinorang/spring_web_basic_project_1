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
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1시간
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 토큰 생성
    public static String generateToken(String username) {
        logger.debug("JWT 토큰 생성 시작 - 사용자: {}", username);
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
        logger.debug("JWT 토큰 생성 완료 - 사용자: {}", username);
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
} 