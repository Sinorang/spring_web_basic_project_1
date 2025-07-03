package com.elice.boardproject.oauth;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.board.repository.BoardRepository;
import com.elice.boardproject.post.repository.PostRepository;
import com.elice.boardproject.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void OAuth_로그인_페이지_접근_가능() throws Exception {
        mockMvc.perform(get("/acc/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("acc/login"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Google로 로그인")));
    }

    @Test
    void OAuth_에러_페이지_접근_가능() throws Exception {
        mockMvc.perform(get("/oauth/error"))
                .andExpect(status().isOk())
                .andExpect(view().name("oauth/error"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("OAuth 로그인 실패")));
    }

    @Test
    void Google_OAuth_인증_요청_리다이렉트() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection());
    }
} 