package com.elice.boardproject.acc.controller;

import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.acc.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.elice.boardproject.board.repository.BoardRepository;
import com.elice.boardproject.post.repository.PostRepository;
import com.elice.boardproject.comment.repository.CommentRepository;
import com.elice.boardproject.acc.dto.UserWithdrawRequestDTO;
import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

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
                .andExpect(status().isUnauthorized()); // 401
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
                .andExpect(status().isNotFound()); // 404
    }

    @Test
    void 비밀번호_불일치_로그인_실패() throws Exception {
        UserDTO loginDTO = new UserDTO();
        loginDTO.setId("testuser");
        loginDTO.setPwd("wrongpass");

        mockMvc.perform(post("/api/acc/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized()); // 401
    }

    @Test
    @WithMockUser(username = "testuser")
    void 사용자_탈퇴_정상_처리_및_사유_저장() throws Exception {
        UserWithdrawRequestDTO dto = new UserWithdrawRequestDTO("테스트 탈퇴 사유");
        mockMvc.perform(patch("/api/acc/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("탈퇴가 완료되었습니다."));
        // DB에서 상태/사유 확인
        User user = userRepository.findByIdForAdmin("testuser");
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(user.getReason()).isEqualTo("테스트 탈퇴 사유");
    }

    @Test
    @WithMockUser(username = "testuser")
    void 사용자_탈퇴_사유_미입력시_null_저장() throws Exception {
        UserWithdrawRequestDTO dto = new UserWithdrawRequestDTO(null);
        mockMvc.perform(patch("/api/acc/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("탈퇴가 완료되었습니다."));
        User user = userRepository.findByIdForAdmin("testuser");
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(user.getReason()).isNull();
    }
} 