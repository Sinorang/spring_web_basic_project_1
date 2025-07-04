package com.elice.boardproject.post;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.service.UserService;
import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.entity.BoardDTO;
import com.elice.boardproject.board.service.BoardService;
import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.service.PostService;
import com.elice.boardproject.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Transactional
public class PostDeleteIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private PostService postService;

    private MockMvc mockMvc;
    private User testUser;
    private Board testBoard;
    private Post testPost;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 테스트 사용자 생성
        String userId = "testuser_" + System.currentTimeMillis();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setPwd("testpass");
        userDTO.setName("테스트사용자");
        userDTO.setNickname("테스트닉");
        userDTO.setEmail("test@example.com");
        userService.join(userDTO);
        testUser = userService.getUserById(userId);

        // JWT 토큰 생성
        jwtToken = JwtUtil.generateToken(testUser.getId());

        // 테스트 게시판 생성
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setName("테스트게시판");
        boardDTO.setDescription("테스트게시판설명");
        boardDTO.setUser(testUser);
        testBoard = boardService.createBoard(boardDTO);

        // 테스트 게시글 생성
        Post post = new Post();
        post.setTitle("테스트게시글");
        post.setContent("테스트게시글내용");
        post.setUser(testUser);
        post.setBoard(testBoard);
        testPost = postService.createPost(post, testBoard.getIdx());
    }

    @Test
    void 포스트_삭제_성공_테스트() throws Exception {
        System.out.println("=== 포스트 삭제 성공 테스트 시작 ===");
        System.out.println("테스트 사용자 ID: " + testUser.getId());
        System.out.println("테스트 게시판 ID: " + testBoard.getIdx());
        System.out.println("테스트 게시글 ID: " + testPost.getId());
        System.out.println("JWT 토큰: " + jwtToken);

        // 1. 게시글 조회 테스트 (Board 정보 확인)
        mockMvc.perform(get("/post/" + testPost.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(view().name("post/post"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("boardIdx"));

        // 2. 게시글 삭제 테스트
        mockMvc.perform(delete("/post/" + testPost.getId() + "/delete")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/board/index/*"));

        System.out.println("=== 포스트 삭제 성공 테스트 완료 ===");
    }

    @Test
    void 포스트_삭제_권한_없음_테스트() throws Exception {
        System.out.println("=== 포스트 삭제 권한 없음 테스트 시작 ===");

        // 다른 사용자 생성
        String otherUserId = "otheruser_" + System.currentTimeMillis();
        UserDTO otherUserDTO = new UserDTO();
        otherUserDTO.setId(otherUserId);
        otherUserDTO.setPwd("otherpass");
        otherUserDTO.setName("다른사용자");
        otherUserDTO.setNickname("다른닉");
        otherUserDTO.setEmail("other@example.com");
        userService.join(otherUserDTO);
        User otherUser = userService.getUserById(otherUserId);

        String otherUserToken = JwtUtil.generateToken(otherUser.getId());

        System.out.println("다른 사용자 ID: " + otherUser.getId());
        System.out.println("다른 사용자 토큰: " + otherUserToken);

        // 다른 사용자로 게시글 삭제 시도
        mockMvc.perform(delete("/post/" + testPost.getId() + "/delete")
                .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/post/" + testPost.getId() + "*"));

        System.out.println("=== 포스트 삭제 권한 없음 테스트 완료 ===");
    }

    @Test
    void 존재하지_않는_포스트_삭제_테스트() throws Exception {
        System.out.println("=== 존재하지 않는 포스트 삭제 테스트 시작 ===");

        Long nonExistentPostId = 99999L;

        mockMvc.perform(delete("/post/" + nonExistentPostId + "/delete")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/board/boards*"));

        System.out.println("=== 존재하지 않는 포스트 삭제 테스트 완료 ===");
    }
} 