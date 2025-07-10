package com.elice.boardproject.oauth.handler;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.oauth.service.OAuthService;
import com.elice.boardproject.security.JwtUtil;
import com.elice.boardproject.security.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final OAuthService oAuthService;
    private final RefreshTokenService refreshTokenService;

    public OAuth2AuthenticationSuccessHandler(OAuthService oAuthService, RefreshTokenService refreshTokenService) {
        this.oAuthService = oAuthService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String provider = getProviderFromRequest(request);
        User user = oAuthService.processOAuthUser(oauth2User, provider);
        
        // JwtUtil을 사용하여 AccessToken 생성
        String accessToken = JwtUtil.generateAccessToken(user.getId());

        Cookie jwtCookie = new Cookie("jwt_token", accessToken);
        jwtCookie.setHttpOnly(false); // JavaScript에서 읽을 수 있도록 false로 설정
        jwtCookie.setSecure(request.isSecure()); // HTTPS 사용 시 true
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(1800); // 30분
        response.addCookie(jwtCookie);
        
        // RefreshToken 생성 및 저장
        refreshTokenService.createRefreshToken(user.getId(), 7); // 7일
        
        // RefreshToken 쿠키 설정
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshTokenService.findByUserId(user.getId()).get().getToken());
        refreshTokenCookie.setHttpOnly(false); // JavaScript에서 읽을 수 있도록 false로 설정
        refreshTokenCookie.setSecure(request.isSecure());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(604800); // 7일
        response.addCookie(refreshTokenCookie);

        response.sendRedirect("/");
    }

    private String getProviderFromRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("google")) return "google";
        if (uri.contains("github")) return "github";
        // 필요시 추가
        return "google";
    }
} 