package com.elice.boardproject.admin.controller;

import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.entity.Permission;
import com.elice.boardproject.acc.entity.UserStatus;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.elice.boardproject.admin.repository.PermissionRepository;
import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.repository.BoardRepository;
import com.elice.boardproject.comment.entity.Comment;
import com.elice.boardproject.comment.repository.CommentRepository;
import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.repository.PostRepository;
import com.elice.boardproject.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import com.elice.boardproject.playlist.entity.Playlist;
import com.elice.boardproject.playlist.repository.PlaylistRepository;

/**
 * 관리자 전용 컨텐츠 관리 API 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminContentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User adminUser;
    private User normalUser;
    private Board testBoard;
    private Post testPost;
    private Comment testComment;
    private String adminJwtToken;
    private String normalUserJwtToken;
    private Playlist testPlaylist;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();

        // 테스트 데이터 정리
        commentRepository.deleteAll();
        postRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
        adminRoleRepository.deleteAll();
        permissionRepository.deleteAll();
        playlistRepository.deleteAll();

        String unique = UUID.randomUUID().toString().substring(0, 8);

        // 관리자 역할 및 권한 생성
        createAdminRoleAndPermissions(unique);

        // 일반 사용자 생성
        normalUser = createTestUser("user_" + unique, "일반사용자", "user_" + unique + "@test.com", false);

        // JWT 토큰 생성
        System.out.println("adminUser ID: " + adminUser.getId());
        System.out.println("adminUser isAdmin: " + adminUser.isAdmin());
        System.out.println("adminUser adminRole: " + (adminUser.getAdminRole() != null ? adminUser.getAdminRole().getRoleName() : "null"));
        
        adminJwtToken = JwtUtil.generateToken(adminUser.getId());
        normalUserJwtToken = JwtUtil.generateToken(normalUser.getId());
        
        System.out.println("adminJwtToken: " + adminJwtToken);

        // 테스트 데이터 생성
        testBoard = createTestBoard(adminUser, "테스트게시판", "테스트용 게시판입니다.");
        testPost = createTestPost(normalUser, testBoard, "테스트게시글", "테스트용 게시글입니다.");
        testComment = createTestComment(normalUser, testPost, "테스트댓글입니다.");
        testPlaylist = createTestPlaylist(normalUser, "테스트플리", "테스트 설명");
    }

    @Test
    @DisplayName("관리자는 모든 게시글을 삭제할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void deletePostAsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/posts/" + testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("관리자 권한으로 게시글이 삭제되었습니다."))
                .andExpect(jsonPath("$.deletedPostId").value(testPost.getId()));
    }

    @Test
    @DisplayName("관리자는 모든 댓글을 삭제할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void deleteCommentAsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/comments/" + testComment.getCommentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("관리자 권한으로 댓글이 삭제되었습니다."))
                .andExpect(jsonPath("$.deletedCommentId").value(testComment.getCommentId()));
    }

    @Test
    @DisplayName("관리자는 모든 플레이리스트를 삭제할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void deletePlaylistAsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/playlists/" + testPlaylist.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("관리자 권한으로 플레이리스트가 삭제되었습니다."))
                .andExpect(jsonPath("$.deletedPlaylistId").value(testPlaylist.getId()));
    }

    @Test
    @DisplayName("일반 사용자는 관리자 삭제 API에 접근할 수 없다")
    @WithMockUser(roles = "USER")
    void deletePostAsAdmin_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/posts/" + testPost.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("일반 사용자는 관리자 플레이리스트 삭제 API에 접근할 수 없다")
    @WithMockUser(roles = "USER")
    void deletePlaylistAsAdmin_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/playlists/" + testPlaylist.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 404를 반환한다")
    @WithMockUser(roles = "ADMIN")
    void deletePostAsAdmin_NotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/posts/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 404를 반환한다")
    @WithMockUser(roles = "ADMIN")
    void deleteCommentAsAdmin_NotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/comments/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트 삭제 시 404를 반환한다")
    @WithMockUser(roles = "ADMIN")
    void deletePlaylistAsAdmin_NotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/playlists/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("관리자는 전체 플레이리스트 통계를 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getPlaylistStatisticsAsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/admin/playlists/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlaylists").isNumber());
    }

    // 헬퍼 메서드들
    private void createAdminRoleAndPermissions(String unique) {
        // ADMIN 역할 생성
        AdminRole adminRole = AdminRole.builder()
                .roleName("ADMIN_" + unique)
                .description("관리자")
                .build();
        adminRole = adminRoleRepository.save(adminRole);

        // 관리자 사용자 생성 및 ADMIN 역할 부여
        adminUser = createTestUser("admin_" + unique, "관리자", "admin_" + unique + "@test.com", true);
        adminUser.setAdminRole(adminRole);
        adminUser.setAdminGrantedAt(LocalDateTime.now());
        adminUser.setAdminGrantedBy("system");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);
    }

    private User createTestUser(String id, String name, String email, boolean isAdmin) {
        User user = User.builder()
                .id(id)
                .pwd("password")
                .name(name)
                .nickname(name + "닉네임")
                .email(email)
                .isAdmin(isAdmin)
                .isActive(true)
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    private Board createTestBoard(User user, String name, String description) {
        Board board = Board.builder()
                .user(user)
                .name(name)
                .description(description)
                .build();
        return boardRepository.save(board);
    }

    private Post createTestPost(User user, Board board, String title, String content) {
        Post post = Post.builder()
                .user(user)
                .board(board)
                .title(title)
                .content(content)
                .build();
        return postRepository.save(post);
    }

    private Comment createTestComment(User user, Post post, String content) {
        Comment comment = new Comment(post, user, content);
        return commentRepository.save(comment);
    }

    private Playlist createTestPlaylist(User owner, String title, String desc) {
        Playlist playlist = Playlist.builder()
                .title(title)
                .description(desc)
                .owner(owner)
                .isPublic(true)
                .build();
        return playlistRepository.save(playlist);
    }
} 