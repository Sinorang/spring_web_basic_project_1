package com.elice.boardproject.acc.controller;

import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.acc.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.elice.boardproject.board.repository.BoardRepository;
import com.elice.boardproject.post.repository.PostRepository;
import com.elice.boardproject.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
        // 테스트용 회원가입
        UserDTO userDTO = new UserDTO();
        userDTO.setId("testuser");
        userDTO.setPwd("testpass");
        userDTO.setName("테스트");
        userDTO.setNickname("테스트닉");
        userDTO.setEmail("test@example.com");
        userService.join(userDTO);
    }

    @Test
    void 로그인_성공시_JWT_토큰_반환() throws Exception {
        UserDTO loginDTO = new UserDTO();
        loginDTO.setId("testuser");
        loginDTO.setPwd("testpass");

        mockMvc.perform(post("/api/acc/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.startsWith("Bearer ")));
    }

    @Test
    void 로그인_실패시_401반환() throws Exception {
        UserDTO loginDTO = new UserDTO();
        loginDTO.setId("testuser");
        loginDTO.setPwd("wrongpass");

        mockMvc.perform(post("/api/acc/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그인_성공시_JWT_쿠키_설정() throws Exception {
        UserDTO loginDTO = new UserDTO();
        loginDTO.setId("testuser");
        loginDTO.setPwd("testpass");

        mockMvc.perform(post("/api/acc/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt_token"))
                .andExpect(cookie().httpOnly("jwt_token", true))
                .andExpect(cookie().path("jwt_token", "/"))
                .andExpect(cookie().maxAge("jwt_token", 3600));
    }

    @Test
    void 로그아웃_성공시_쿠키_삭제() throws Exception {
        mockMvc.perform(post("/api/acc/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt_token"));
    }

    @Test
    void 존재하지_않는_사용자_로그인_실패() throws Exception {
        UserDTO loginDTO = new UserDTO();
        loginDTO.setId("nonexistent");
        loginDTO.setPwd("testpass");

        mockMvc.perform(post("/api/acc/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }
} 