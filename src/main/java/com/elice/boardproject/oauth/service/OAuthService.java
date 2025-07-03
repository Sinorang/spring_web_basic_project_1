package com.elice.boardproject.oauth.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class OAuthService {

    private final UserRepository userRepository;

    @Autowired
    public OAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * OAuth 사용자 정보를 처리하여 기존 사용자 조회 또는 신규 생성
     * @param oauth2User OAuth2User 객체
     * @param provider OAuth 제공자 (google, github 등)
     * @return 처리된 User 객체
     */
    public User processOAuthUser(OAuth2User oauth2User, String provider) {
        String oauthId = oauth2User.getName();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // 1. OAuth ID로 기존 사용자 조회
        User existingUser = userRepository.findByOauthProviderAndOauthId(provider, oauthId);
        if (existingUser != null) {
            return existingUser;
        }

        // 2. 이메일로 기존 사용자 조회 (OAuth 계정 연동)
        if (email != null) {
            existingUser = userRepository.findByEmail(email);
            if (existingUser != null) {
                // 기존 사용자에 OAuth 정보 추가
                existingUser.setOauthProvider(provider);
                existingUser.setOauthId(oauthId);
                existingUser.setOauthEmail(email);
                return userRepository.save(existingUser);
            }
        }

        // 3. 신규 사용자 생성
        String userId = "oauth_" + provider + "_" + oauthId;
        String displayName = name != null ? name : "OAuth 사용자";
        String nickname = name != null ? name : "OAuth사용자";

        User newUser = User.builder()
                .id(userId)
                .pwd("") // OAuth 사용자는 비밀번호 없음
                .name(displayName)
                .nickname(nickname)
                .email(email != null ? email : userId + "@oauth.local")
                .oauthProvider(provider)
                .oauthId(oauthId)
                .oauthEmail(email)
                .build();

        return userRepository.save(newUser);
    }
} 