package com.elice.boardproject.controller;

import com.elice.boardproject.security.JwtUtil;
import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.acc.service.UserService;
import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.entity.BoardDTO;
import com.elice.boardproject.board.service.BoardService;
import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.entity.PostDTO;
import com.elice.boardproject.post.service.PostService;
import com.elice.boardproject.board.repository.BoardRepository;
import com.elice.boardproject.post.repository.PostRepository;
import com.elice.boardproject.comment.repository.CommentRepository;
import com.elice.boardproject.admin.service.AdminUserService;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.repository.PermissionRepository;
import com.elice.boardproject.admin.entity.Permission;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void setUp() {
        // 'ADMIN' 역할이 없으면 생성
        AdminRole adminRole = adminRoleRepository.findByRoleName("ADMIN").orElseGet(() -> {
            AdminRole role = new AdminRole();
            role.setRoleName("ADMIN");
            role.setDescription("관리자");
            return adminRoleRepository.save(role);
        });
        // 'BOARD_MANAGE' 권한이 없으면 생성
        Permission boardManage = permissionRepository.findByPermissionName("BOARD_MANAGE").orElseGet(() -> {
            Permission p = new Permission();
            p.setPermissionName("BOARD_MANAGE");
            p.setDescription("게시판 관리");
            return permissionRepository.save(p);
        });
        // ADMIN 역할에 BOARD_MANAGE 권한이 없으면 추가
        if (adminRole.getPermissions() == null || adminRole.getPermissions().stream().noneMatch(p -> p.getPermissionName().equals("BOARD_MANAGE"))) {
            adminRole.getPermissions().add(boardManage);
            adminRoleRepository.save(adminRole);
        }
        commentRepository.deleteAll();
        postRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    private TestData createTestData(String userId) {
        // 테스트용 사용자 생성
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setPwd("testpass");
        userDTO.setName("테스트");
        userDTO.setNickname("테스트닉");
        userDTO.setEmail(userId + "@example.com");
        userService.join(userDTO);
        
        User testUser = userService.getUserById(userId);
        String jwtToken = JwtUtil.generateToken(userId);
        assert testUser != null && testUser.getIdx() != null;
        
        // 테스트용 게시판 생성
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setUser(testUser);
        boardDTO.setName("테스트 게시판");
        boardDTO.setDescription("테스트용 게시판입니다.");
        Board testBoard = boardService.createBoard(boardDTO);
        assert testBoard != null && testBoard.getIdx() != null && testBoard.getUser() != null;
        
        // 테스트용 게시글 생성
        Post post = new Post();
        post.setTitle("테스트 게시글");
        post.setContent("테스트용 게시글 내용입니다.");
        post.setUser(testUser);
        post.setBoard(testBoard);
        Post testPost = postService.createPost(post, testBoard.getIdx());
        assert testPost != null && testPost.getId() != null && testPost.getUser() != null && testPost.getBoard() != null;
        
        return new TestData(testUser, jwtToken, testBoard, testPost);
    }

    static class TestData {
        User user;
        String jwtToken;
        Board board;
        Post post;

        TestData(User user, String jwtToken, Board board, Post post) {
            this.user = user;
            this.jwtToken = jwtToken;
            this.board = board;
            this.post = post;
        }
    }

    @Test
    void JWT_토큰으로_게시판_목록_조회() throws Exception {
        TestData data = createTestData("user1_" + System.currentTimeMillis());
        mockMvc.perform(get("/board/boards")
                .header("Authorization", "Bearer " + data.jwtToken))
                .andExpect(status().isOk())
                .andExpect(view().name("board/boards"));
    }

    @Test
    void JWT_토큰으로_게시글_조회() throws Exception {
        TestData data = createTestData("user2_" + System.currentTimeMillis());
        mockMvc.perform(get("/post/" + data.post.getId())
                .header("Authorization", "Bearer " + data.jwtToken))
                .andExpect(status().isOk())
                .andExpect(view().name("post/post"));
    }

    @Test
    void JWT_쿠키로_게시판_목록_조회() throws Exception {
        TestData data = createTestData("user3_" + System.currentTimeMillis());
        mockMvc.perform(get("/board/boards")
                .cookie(new jakarta.servlet.http.Cookie("jwt_token", data.jwtToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("board/boards"));
    }

    @Test
    void JWT_쿠키로_게시글_조회() throws Exception {
        TestData data = createTestData("user4_" + System.currentTimeMillis());
        mockMvc.perform(get("/post/" + data.post.getId())
                .cookie(new jakarta.servlet.http.Cookie("jwt_token", data.jwtToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("post/post"));
    }

    @Test
    void 토큰_없이_게시판_목록_조회_성공() throws Exception {
        mockMvc.perform(get("/board/boards"))
                .andExpect(status().isOk())
                .andExpect(view().name("board/boards"));
    }

    @Test
    void 토큰_없이_게시글_조회_실패() throws Exception {
        TestData data = createTestData("user5_" + System.currentTimeMillis());
        mockMvc.perform(get("/post/" + data.post.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acc/login-required"));
    }

    @Test
    void 무효한_토큰으로_게시글_조회_실패() throws Exception {
        TestData data = createTestData("user5_" + System.currentTimeMillis());
        mockMvc.perform(get("/post/" + data.post.getId())
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/acc/login-required"));
    }

    @Test
    void JWT_토큰으로_게시글_생성_페이지_접근() throws Exception {
        TestData data = createTestData("user6_" + System.currentTimeMillis());
        mockMvc.perform(get("/post/create")
                .param("boardIdx", data.board.getIdx().toString())
                .header("Authorization", "Bearer " + data.jwtToken))
                .andExpect(status().isOk())
                .andExpect(view().name("post/createPost"));
    }

    @Test
    void JWT_토큰으로_게시판_생성_페이지_접근() throws Exception {
        TestData data = createTestData("user7_" + System.currentTimeMillis());
        // 테스트 유저에게 BOARD_MANAGE 권한 부여 (테스트용으로 직접 설정)
        data.user.setAdmin(true);
        AdminRole adminRole = adminRoleRepository.findByRoleName("ADMIN").orElseThrow();
        data.user.setAdminRole(adminRole);
        data.user.setAdminGrantedAt(LocalDateTime.now());
        data.user.setAdminGrantedBy("test");
        userRepository.save(data.user);
        mockMvc.perform(get("/board/create")
                .header("Authorization", "Bearer " + data.jwtToken))
                .andExpect(status().isOk())
                .andExpect(view().name("board/createBoard"));
    }

    @Test
    void JWT_토큰으로_게시글_생성() throws Exception {
        TestData data = createTestData("user8_" + System.currentTimeMillis());
        PostDTO postDTO = new PostDTO();
        postDTO.setTitle("새 게시글");
        postDTO.setContent("새 게시글 내용");

        mockMvc.perform(post("/post/create")
                .param("boardIdx", data.board.getIdx().toString())
                .param("title", postDTO.getTitle())
                .param("content", postDTO.getContent())
                .header("Authorization", "Bearer " + data.jwtToken))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void JWT_토큰으로_게시판_생성() throws Exception {
        TestData data = createTestData("user9_" + System.currentTimeMillis());
        // 테스트 유저에게 BOARD_MANAGE 권한 부여 (테스트용으로 직접 설정)
        data.user.setAdmin(true);
        AdminRole adminRole = adminRoleRepository.findByRoleName("ADMIN").orElseThrow();
        data.user.setAdminRole(adminRole);
        data.user.setAdminGrantedAt(LocalDateTime.now());
        data.user.setAdminGrantedBy("test");
        userRepository.save(data.user);
        mockMvc.perform(post("/board/create")
                .param("name", "새 게시판")
                .param("description", "새 게시판 설명")
                .header("Authorization", "Bearer " + data.jwtToken))
                .andExpect(status().is3xxRedirection());
    }
} 