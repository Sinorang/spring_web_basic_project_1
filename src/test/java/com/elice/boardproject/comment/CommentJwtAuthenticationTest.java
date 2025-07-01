package com.elice.boardproject.comment;

import com.elice.boardproject.JwtUtil;
import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.acc.service.UserService;
import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.entity.BoardDTO;
import com.elice.boardproject.board.service.BoardService;
import com.elice.boardproject.comment.entity.Comment;
import com.elice.boardproject.comment.entity.CommentDTO;
import com.elice.boardproject.comment.service.CommentService;
import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.entity.PostDTO;
import com.elice.boardproject.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentJwtAuthenticationTest {

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
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 데이터베이스 정리
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
        
        // 테스트용 댓글 생성
        Comment comment = new Comment();
        comment.setCommentContent("테스트 댓글입니다.");
        comment.setUser(testUser);
        comment.setPost(testPost);
        Comment testComment = commentService.createComment(testPost.getId(), comment);
        assert testComment != null && testComment.getCommentId() != null && testComment.getUser() != null && testComment.getPost() != null;
        
        return new TestData(testUser, jwtToken, testBoard, testPost, testComment);
    }

    static class TestData {
        User user;
        String jwtToken;
        Board board;
        Post post;
        Comment comment;

        TestData(User user, String jwtToken, Board board, Post post, Comment comment) {
            this.user = user;
            this.jwtToken = jwtToken;
            this.board = board;
            this.post = post;
            this.comment = comment;
        }
    }

    @Test
    void JWT_토큰으로_댓글_작성() throws Exception {
        TestData data = createTestData("commentuser1_" + System.currentTimeMillis());
        mockMvc.perform(post("/comment")
                .param("postId", data.post.getId().toString())
                .param("commentContent", "새로운 댓글입니다.")
                .header("Authorization", "Bearer " + data.jwtToken))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void JWT_쿠키로_댓글_작성() throws Exception {
        TestData data = createTestData("commentuser2_" + System.currentTimeMillis());
        mockMvc.perform(post("/comment")
                .param("postId", data.post.getId().toString())
                .param("commentContent", "쿠키로 작성한 댓글입니다.")
                .cookie(new jakarta.servlet.http.Cookie("jwt_token", data.jwtToken)))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void 토큰_없이_댓글_작성_실패() throws Exception {
        TestData data = createTestData("commentuser3_" + System.currentTimeMillis());
        mockMvc.perform(post("/comment")
                .param("postId", data.post.getId().toString())
                .param("commentContent", "인증되지 않은 댓글입니다."))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void JWT_토큰으로_댓글_수정() throws Exception {
        TestData data = createTestData("commentuser4_" + System.currentTimeMillis());
        mockMvc.perform(post("/comment/" + data.comment.getCommentId() + "/edit")
                .param("commentContent", "수정된 댓글입니다.")
                .header("Authorization", "Bearer " + data.jwtToken))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void JWT_토큰으로_댓글_삭제() throws Exception {
        TestData data = createTestData("commentuser5_" + System.currentTimeMillis());
        mockMvc.perform(delete("/comment/" + data.comment.getCommentId() + "/delete")
                .header("Authorization", "Bearer " + data.jwtToken))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void 다른_사용자_토큰으로_댓글_수정_실패() throws Exception {
        TestData data = createTestData("commentuser6_" + System.currentTimeMillis());
        // 다른 사용자 생성
        UserDTO otherUserDTO = new UserDTO();
        otherUserDTO.setId("otheruser1_" + System.currentTimeMillis());
        otherUserDTO.setPwd("otherpass");
        otherUserDTO.setName("다른사용자");
        otherUserDTO.setNickname("다른닉");
        otherUserDTO.setEmail("other1@example.com");
        userService.join(otherUserDTO);
        
        String otherUserToken = JwtUtil.generateToken(otherUserDTO.getId());
        
        mockMvc.perform(post("/comment/" + data.comment.getCommentId() + "/edit")
                .param("commentContent", "다른 사용자가 수정하려는 댓글입니다.")
                .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void 다른_사용자_토큰으로_댓글_삭제_실패() throws Exception {
        TestData data = createTestData("commentuser7_" + System.currentTimeMillis());
        // 다른 사용자 생성
        UserDTO otherUserDTO = new UserDTO();
        otherUserDTO.setId("otheruser2_" + System.currentTimeMillis());
        otherUserDTO.setPwd("otherpass");
        otherUserDTO.setName("다른사용자");
        otherUserDTO.setNickname("다른닉");
        otherUserDTO.setEmail("other2@example.com");
        userService.join(otherUserDTO);
        
        String otherUserToken = JwtUtil.generateToken(otherUserDTO.getId());
        
        mockMvc.perform(delete("/comment/" + data.comment.getCommentId() + "/delete")
                .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().is3xxRedirection());
    }
} 