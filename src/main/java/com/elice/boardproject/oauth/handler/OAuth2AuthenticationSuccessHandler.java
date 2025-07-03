package com.elice.boardproject.oauth.handler;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.oauth.service.OAuthService;
import com.elice.boardproject.security.JwtUtil;
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

    public OAuth2AuthenticationSuccessHandler(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String provider = getProviderFromRequest(request);
        User user = oAuthService.processOAuthUser(oauth2User, provider);
        String token = JwtUtil.generateToken(user.getId());

        Cookie jwtCookie = new Cookie("jwt_token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // 운영 환경에서는 true
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);
        response.addCookie(jwtCookie);

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