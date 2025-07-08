package com.elice.boardproject.playlist;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.playlist.entity.Playlist;
import com.elice.boardproject.playlist.service.PlaylistService;
import com.elice.boardproject.security.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private User testUser;
    private ObjectMapper objectMapper;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new User();
        testUser.setId("testuser");
        testUser.setName("테스트 사용자");
        testUser.setNickname("테스트닉");
        testUser.setEmail("test@example.com");
        testUser.setPwd("password");
        userRepository.save(testUser);

        // JWT 토큰 생성
        jwtToken = generateJwtToken(testUser.getId());

        objectMapper = new ObjectMapper();
    }

    private String generateJwtToken(String userId) {
        SecretKey key = Keys.hmacShaKeyFor("mysecretkeymysecretkeymysecretkeymysecretkey".getBytes());
        return Jwts.builder()
                .claim("sub", userId)
                .signWith(key)
                .compact();
    }

    @Test
    void 플레이리스트_생성_페이지_접근_성공() throws Exception {
        mockMvc.perform(get("/playlist/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("playlist/create"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("플레이리스트 생성")));
    }

    @Test
    void 플레이리스트_목록_페이지_접근_성공() throws Exception {
        mockMvc.perform(get("/playlist/list")
                        .cookie(new jakarta.servlet.http.Cookie("jwt_token", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("playlist/list"));
    }

    @Test
    void 플레이리스트_상세_페이지_접근_성공() throws Exception {
        // given: 테스트 플레이리스트 생성
        String youtubeUrl = "https://music.youtube.com/playlist?list=PLdWdCc1yLsnH-iCVckG-l0EV5APQGi4jj&si=umG4km0OVifUNSLw";
        Playlist playlist = playlistService.createPlaylistFromYoutubeUrl(youtubeUrl, testUser);

        // when & then
        mockMvc.perform(get("/playlist/" + playlist.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("playlist/detail"));
    }

    @Test
    void 존재하지_않는_플레이리스트_상세_페이지_접근_실패() throws Exception {
        mockMvc.perform(get("/playlist/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/playlist/list"));
    }

    @Test
    void 플레이리스트_생성_API_성공() throws Exception {
        // given
        String requestBody = objectMapper.writeValueAsString(new PlaylistCreateRequest(
                "https://music.youtube.com/playlist?list=PLdWdCc1yLsnH-iCVckG-l0EV5APQGi4jj&si=umG4km0OVifUNSLw"
        ));

        // when & then
        mockMvc.perform(post("/api/playlist/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new jakarta.servlet.http.Cookie("jwt_token", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("플레이리스트가 성공적으로 생성되었습니다."));
    }

    @Test
    void 잘못된_URL로_플레이리스트_생성_API_실패() throws Exception {
        // given
        String requestBody = objectMapper.writeValueAsString(new PlaylistCreateRequest(
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        ));

        // when & then
        mockMvc.perform(post("/api/playlist/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .cookie(new jakarta.servlet.http.Cookie("jwt_token", jwtToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 YouTube 플레이리스트 URL입니다."));
    }

    @Test
    void 플레이리스트_삭제_API_성공() throws Exception {
        // given: 테스트 플레이리스트 생성
        String youtubeUrl = "https://music.youtube.com/playlist?list=PLdWdCc1yLsnH-iCVckG-l0EV5APQGi4jj&si=umG4km0OVifUNSLw";
        Playlist playlist = playlistService.createPlaylistFromYoutubeUrl(youtubeUrl, testUser);

        // when & then
        mockMvc.perform(delete("/api/playlist/" + playlist.getId())
                        .cookie(new jakarta.servlet.http.Cookie("jwt_token", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("플레이리스트가 성공적으로 삭제되었습니다."));
    }

    @Test
    void 존재하지_않는_플레이리스트_삭제_API_실패() throws Exception {
        mockMvc.perform(delete("/api/playlist/999")
                        .cookie(new jakarta.servlet.http.Cookie("jwt_token", jwtToken)))
                .andExpect(status().isNotFound());
    }

    // 내부 클래스: 요청 DTO
    private static class PlaylistCreateRequest {
        private String youtubeUrl;

        public PlaylistCreateRequest() {}

        public PlaylistCreateRequest(String youtubeUrl) {
            this.youtubeUrl = youtubeUrl;
        }

        public String getYoutubeUrl() {
            return youtubeUrl;
        }

        public void setYoutubeUrl(String youtubeUrl) {
            this.youtubeUrl = youtubeUrl;
        }
    }
} 