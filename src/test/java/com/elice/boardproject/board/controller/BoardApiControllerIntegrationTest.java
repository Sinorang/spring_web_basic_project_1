package com.elice.boardproject.board.controller;

import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.entity.Permission;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.elice.boardproject.admin.repository.PermissionRepository;
import com.elice.boardproject.admin.service.AdminUserService;
import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.entity.BoardDTO;
import com.elice.boardproject.board.repository.BoardRepository;
import com.elice.boardproject.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

/**
 * 게시판 권한별 API 컨트롤러 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BoardApiControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private com.elice.boardproject.post.repository.PostRepository postRepository;

    @Autowired
    private com.elice.boardproject.comment.repository.CommentRepository commentRepository;

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AdminUserService adminUserService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;
    private User adminUser;
    private Board testBoard;
    private String testUserJwtToken;
    private String adminUserJwtToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        // 테스트 데이터 정리 (외래키 제약조건 고려)
        commentRepository.deleteAll();
        postRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
        adminRoleRepository.deleteAll();
        permissionRepository.deleteAll();

        String unique = UUID.randomUUID().toString().substring(0, 8);

        // 권한 및 역할 초기화
        initializePermissionsAndRoles(unique);

        // 테스트 사용자 생성
        testUser = createTestUser("testuser_" + unique, "테스트유저", "test_" + unique + "@example.com");
        adminUser = createTestUser("admin_" + unique, "관리자", "admin_" + unique + "@example.com");

        // 관리자 권한 부여 (테스트용으로 슈퍼 관리자 권한을 가진 사용자로 설정)
        // 실제로는 슈퍼 관리자가 권한을 부여해야 하지만, 테스트에서는 직접 설정
        adminUser.setAdmin(true);
        AdminRole adminRole = adminRoleRepository.findByRoleName("ADMIN_" + unique).orElseThrow();
        adminUser.setAdminRole(adminRole);
        adminUser.setAdminGrantedAt(LocalDateTime.now());
        adminUser.setAdminGrantedBy("test");
        userRepository.save(adminUser);
        // ADMIN 역할에 게시판 관련 권한 직접 매핑
        for (String perm : new String[]{"BOARD_READ", "BOARD_CREATE", "BOARD_UPDATE", "BOARD_DELETE"}) {
            Permission permission = permissionRepository.findByPermissionName(perm).orElseThrow();
            if (!adminRole.getPermissions().contains(permission)) {
                adminRole.getPermissions().add(permission);
            }
        }
        adminRoleRepository.save(adminRole);

        // JWT 토큰 생성
        testUserJwtToken = JwtUtil.generateToken(testUser.getId());
        adminUserJwtToken = JwtUtil.generateToken(adminUser.getId());

        // 테스트 게시판 생성
        testBoard = createTestBoard(adminUser, "테스트게시판", "테스트용 게시판입니다.");
    }

    @Test
    void 게시판_목록_조회_성공() throws Exception {
        mockMvc.perform(get("/api/boards")
                .header("Authorization", "Bearer " + adminUserJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.message").value("게시판 목록을 성공적으로 조회했습니다."));
    }

    @Test
    void 게시판_목록_조회_권한_없음() throws Exception {
        mockMvc.perform(get("/api/boards")
                .header("Authorization", "Bearer " + testUserJwtToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void 특정_게시판_조회_성공() throws Exception {
        mockMvc.perform(get("/api/boards/" + testBoard.getIdx())
                .header("Authorization", "Bearer " + adminUserJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.idx").value(testBoard.getIdx()))
                .andExpect(jsonPath("$.message").value("게시판을 성공적으로 조회했습니다."));
    }

    @Test
    void 존재하지_않는_게시판_조회() throws Exception {
        mockMvc.perform(get("/api/boards/99999")
                .header("Authorization", "Bearer " + adminUserJwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void 게시판_생성_성공() throws Exception {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setName("새게시판");
        boardDTO.setDescription("새로 생성한 게시판입니다.");

        mockMvc.perform(post("/api/boards")
                .header("Authorization", "Bearer " + adminUserJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boardDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("새게시판"))
                .andExpect(jsonPath("$.message").value("게시판이 성공적으로 생성되었습니다."));
    }

    @Test
    void 게시판_생성_권한_없음() throws Exception {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setName("새게시판");
        boardDTO.setDescription("새로 생성한 게시판입니다.");

        mockMvc.perform(post("/api/boards")
                .header("Authorization", "Bearer " + testUserJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boardDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 게시판_수정_성공() throws Exception {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setName("수정된게시판");
        boardDTO.setDescription("수정된 게시판입니다.");

        mockMvc.perform(put("/api/boards/" + testBoard.getIdx())
                .header("Authorization", "Bearer " + adminUserJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boardDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("수정된게시판"))
                .andExpect(jsonPath("$.message").value("게시판이 성공적으로 수정되었습니다."));
    }

    @Test
    void 게시판_수정_권한_없음() throws Exception {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setName("수정된게시판");
        boardDTO.setDescription("수정된 게시판입니다.");

        mockMvc.perform(put("/api/boards/" + testBoard.getIdx())
                .header("Authorization", "Bearer " + testUserJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boardDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 게시판_삭제_성공() throws Exception {
        mockMvc.perform(delete("/api/boards/" + testBoard.getIdx())
                .header("Authorization", "Bearer " + adminUserJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시판이 성공적으로 삭제되었습니다."));
    }

    @Test
    void 게시판_삭제_권한_없음() throws Exception {
        mockMvc.perform(delete("/api/boards/" + testBoard.getIdx())
                .header("Authorization", "Bearer " + testUserJwtToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void 게시판_통계_조회_성공() throws Exception {
        mockMvc.perform(get("/api/boards/" + testBoard.getIdx() + "/statistics")
                .header("Authorization", "Bearer " + adminUserJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.boardIdx").value(testBoard.getIdx()))
                .andExpect(jsonPath("$.data.boardName").value("테스트게시판"))
                .andExpect(jsonPath("$.message").value("게시판 통계를 성공적으로 조회했습니다."));
    }

    @Test
    void 게시판_통계_조회_권한_없음() throws Exception {
        mockMvc.perform(get("/api/boards/" + testBoard.getIdx() + "/statistics")
                .header("Authorization", "Bearer " + testUserJwtToken))
                .andExpect(status().isForbidden());
    }

    // 헬퍼 메서드들
    private void initializePermissionsAndRoles(String unique) {
        // 게시판 관련 권한 생성 (UUID suffix 없이)
        String[] permissions = {"BOARD_READ", "BOARD_CREATE", "BOARD_UPDATE", "BOARD_DELETE"};
        for (String permissionName : permissions) {
            if (permissionRepository.findByPermissionName(permissionName).isEmpty()) {
                Permission permission = new Permission();
                permission.setPermissionName(permissionName);
                permission.setDescription(permissionName + " 권한");
                permissionRepository.save(permission);
            }
        }

        // ADMIN 역할 생성
        if (adminRoleRepository.findByRoleName("ADMIN_" + unique).isEmpty()) {
            AdminRole adminRole = new AdminRole();
            adminRole.setRoleName("ADMIN_" + unique);
            adminRole.setDescription("관리자");
            adminRoleRepository.save(adminRole);
        }
    }

    private User createTestUser(String id, String name, String email) {
        User user = User.builder()
                .id(id)
                .pwd("password")
                .name(name)
                .nickname(id)
                .email(email)
                .isActive(true)
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
} 