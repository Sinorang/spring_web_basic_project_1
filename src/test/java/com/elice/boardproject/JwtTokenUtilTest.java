package com.elice.boardproject;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenUtilTest {

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtTokenUtil jwtTokenUtil;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "hashedpassword", "테스트", "테스트닉", "test@example.com");
        validToken = JwtUtil.generateToken("testuser");
    }

    @Test
    void 유효한_토큰에서_사용자ID_추출() {
        String userId = jwtTokenUtil.getUserIdFromToken(validToken);
        assertThat(userId).isEqualTo("testuser");
    }

    @Test
    void Bearer_접두사가_있는_토큰에서_사용자ID_추출() {
        String bearerToken = "Bearer " + validToken;
        String userId = jwtTokenUtil.getUserIdFromToken(bearerToken);
        assertThat(userId).isEqualTo("testuser");
    }

    @Test
    void Authorization_헤더에서_현재_사용자_가져오기() {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(userService.getUserById("testuser")).thenReturn(testUser);

        User currentUser = jwtTokenUtil.getCurrentUser(request);

        assertThat(currentUser).isNotNull();
        assertThat(currentUser.getId()).isEqualTo("testuser");
    }

    @Test
    void 쿠키에서_현재_사용자_가져오기() {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie("jwt_token", validToken)
        });
        when(userService.getUserById("testuser")).thenReturn(testUser);

        User currentUser = jwtTokenUtil.getCurrentUser(request);

        assertThat(currentUser).isNotNull();
        assertThat(currentUser.getId()).isEqualTo("testuser");
    }

    @Test
    void 토큰이_없을_때_null_반환() {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        User currentUser = jwtTokenUtil.getCurrentUser(request);

        assertThat(currentUser).isNull();
    }

    @Test
    void 유효한_토큰_검증() {
        boolean isValid = jwtTokenUtil.isValidToken(validToken);
        assertThat(isValid).isTrue();
    }

    @Test
    void Bearer_접두사가_있는_유효한_토큰_검증() {
        boolean isValid = jwtTokenUtil.isValidToken("Bearer " + validToken);
        assertThat(isValid).isTrue();
    }

    @Test
    void 무효한_토큰_검증() {
        boolean isValid = jwtTokenUtil.isValidToken("invalid.token.here");
        assertThat(isValid).isFalse();
    }

    @Test
    void null_토큰_검증() {
        boolean isValid = jwtTokenUtil.isValidToken(null);
        assertThat(isValid).isFalse();
    }
} 