package com.elice.boardproject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkeymysecretkey"; // 256bit 이상 권장
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1시간
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 토큰 생성
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 사용자명 추출
    public static String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 토큰 유효성 검증
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 