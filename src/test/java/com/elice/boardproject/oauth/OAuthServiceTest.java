package com.elice.boardproject.oauth;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.board.repository.BoardRepository;
import com.elice.boardproject.post.repository.PostRepository;
import com.elice.boardproject.comment.repository.CommentRepository;
import com.elice.boardproject.oauth.service.OAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class OAuthServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private OAuthService oAuthService;

    @BeforeEach
    void setUp() {
        // 외래키 제약조건을 고려한 삭제 순서
        commentRepository.deleteAll();
        postRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void OAuth_서비스_빈_생성_확인() {
        assertThat(userRepository).isNotNull();
        assertThat(oAuthService).isNotNull();
    }

    @Test
    void OAuth_사용자_저장_테스트() {
        // Given
        User oauthUser = User.builder()
                .id("oauth_google_123456789")
                .pwd("")
                .name("OAuth 테스트 사용자")
                .nickname("OAuth테스트1")
                .email("oauth123456789@test.com")
                .oauthProvider("google")
                .oauthId("123456789")
                .oauthEmail("oauth123456789@test.com")
                .build();

        // When
        User savedUser = userRepository.save(oauthUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getOauthProvider()).isEqualTo("google");
        assertThat(savedUser.getOauthId()).isEqualTo("123456789");
        assertThat(savedUser.getOauthEmail()).isEqualTo("oauth123456789@test.com");
    }

    @Test
    void OAuth_사용자_조회_테스트() {
        // Given
        User oauthUser = User.builder()
                .id("oauth_google_987654321")
                .pwd("")
                .name("OAuth 테스트 사용자2")
                .nickname("OAuth테스트2")
                .email("oauth987654321@test.com")
                .oauthProvider("google")
                .oauthId("987654321")
                .oauthEmail("oauth987654321@test.com")
                .build();
        userRepository.save(oauthUser);

        // When
        User foundUser = userRepository.findByOauthProviderAndOauthId("google", "987654321");

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo("oauth_google_987654321");
        assertThat(foundUser.getOauthProvider()).isEqualTo("google");
    }

    @Test
    void 이메일로_사용자_조회_테스트() {
        // Given
        User oauthUser = User.builder()
                .id("oauth_google_555555555")
                .pwd("")
                .name("OAuth 테스트 사용자3")
                .nickname("OAuth테스트3")
                .email("oauth555555555@test.com")
                .oauthProvider("google")
                .oauthId("555555555")
                .oauthEmail("oauth555555555@test.com")
                .build();
        userRepository.save(oauthUser);

        // When
        User foundUser = userRepository.findByEmail("oauth555555555@test.com");

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("oauth555555555@test.com");
    }

    @Test
    void 신규_Google_OAuth_사용자_처리_성공() {
        // Given
        OAuth2User oauth2User = createMockOAuth2User("google", "123456789", "test@example.com", "테스트 사용자");
        
        // When
        User result = oAuthService.processOAuthUser(oauth2User, "google");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOauthProvider()).isEqualTo("google");
        assertThat(result.getOauthId()).isEqualTo("123456789");
        assertThat(result.getOauthEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("테스트 사용자");
        assertThat(result.getId()).startsWith("oauth_google_");
    }

    @Test
    void 기존_Google_OAuth_사용자_재로그인_성공() {
        // Given
        User existingUser = User.builder()
                .id("oauth_google_123456789")
                .pwd("")
                .name("테스트 사용자")
                .nickname("테스트닉")
                .email("test@example.com")
                .oauthProvider("google")
                .oauthId("123456789")
                .oauthEmail("test@example.com")
                .build();
        userRepository.save(existingUser);

        OAuth2User oauth2User = createMockOAuth2User("google", "123456789", "test@example.com", "테스트 사용자");
        
        // When
        User result = oAuthService.processOAuthUser(oauth2User, "google");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOauthId()).isEqualTo("123456789");
        assertThat(result.getId()).isEqualTo("oauth_google_123456789");
    }

    @Test
    void 이메일_기반_기존_사용자와_OAuth_계정_연동() {
        // Given - 기존 일반 회원가입 사용자
        User existingUser = User.builder()
                .id("existinguser")
                .pwd("password")
                .name("기존 사용자")
                .nickname("기존닉")
                .email("test@example.com")
                .build();
        userRepository.save(existingUser);

        OAuth2User oauth2User = createMockOAuth2User("google", "123456789", "test@example.com", "테스트 사용자");
        
        // When
        User result = oAuthService.processOAuthUser(oauth2User, "google");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getOauthProvider()).isEqualTo("google");
        assertThat(result.getOauthId()).isEqualTo("123456789");
    }

    private OAuth2User createMockOAuth2User(String provider, String oauthId, String email, String name) {
        OAuth2User oauth2User = mock(OAuth2User.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", oauthId);
        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("given_name", name.split(" ")[0]);
        attributes.put("family_name", name.split(" ").length > 1 ? name.split(" ")[1] : "");
        attributes.put("picture", "https://example.com/photo.jpg");
        
        when(oauth2User.getName()).thenReturn(oauthId);
        when(oauth2User.getAttributes()).thenReturn(attributes);
        when(oauth2User.getAttribute("sub")).thenReturn(oauthId);
        when(oauth2User.getAttribute("email")).thenReturn(email);
        when(oauth2User.getAttribute("name")).thenReturn(name);
        
        return oauth2User;
    }
} 