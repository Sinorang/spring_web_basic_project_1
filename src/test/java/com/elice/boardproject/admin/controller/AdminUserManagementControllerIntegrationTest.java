package com.elice.boardproject.admin.controller;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserStatus;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

/**
 * 관리자 전용 사용자 관리 API 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminUserManagementControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User adminUser;
    private User normalUser;
    private User suspendedUser;
    private User withdrawnUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();

        // 테스트 데이터 정리
        userRepository.deleteAll();
        adminRoleRepository.deleteAll();

        String unique = UUID.randomUUID().toString().substring(0, 8);

        // 관리자 역할 생성
        AdminRole adminRole = AdminRole.builder()
                .roleName("ADMIN_" + unique)
                .description("관리자")
                .build();
        adminRole = adminRoleRepository.save(adminRole);

        // 테스트 사용자 생성
        adminUser = createTestUser("admin_" + unique, "관리자", "admin_" + unique + "@test.com", true, UserStatus.ACTIVE);
        adminUser.setAdminRole(adminRole);
        adminUser.setAdminGrantedAt(LocalDateTime.now());
        adminUser.setAdminGrantedBy("system");
        adminUser = userRepository.save(adminUser);

        normalUser = createTestUser("user_" + unique, "일반사용자", "user_" + unique + "@test.com", false, UserStatus.ACTIVE);
        suspendedUser = createTestUser("suspended_" + unique, "정지사용자", "suspended_" + unique + "@test.com", false, UserStatus.SUSPENDED);
        withdrawnUser = createTestUser("withdrawn_" + unique, "탈퇴사용자", "withdrawn_" + unique + "@test.com", false, UserStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("관리자는 전체 사용자 목록을 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Success() throws Exception {
        mockMvc.perform(get("/api/admin/user-management"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].status").exists());
    }

    @Test
    @DisplayName("관리자는 특정 사용자 상세 정보를 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getUserDetail_Success() throws Exception {
        mockMvc.perform(get("/api/admin/user-management/" + normalUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(normalUser.getId()))
                .andExpect(jsonPath("$.name").value(normalUser.getName()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("관리자는 사용자 계정을 정지할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void suspendUser_Success() throws Exception {
        mockMvc.perform(put("/api/admin/user-management/" + normalUser.getId() + "/suspend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 계정이 정지되었습니다."))
                .andExpect(jsonPath("$.userId").value(normalUser.getId()));
    }

    @Test
    @DisplayName("관리자는 정지된 사용자 계정을 활성화할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void activateUser_Success() throws Exception {
        mockMvc.perform(put("/api/admin/user-management/" + suspendedUser.getId() + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 계정이 활성화되었습니다."))
                .andExpect(jsonPath("$.userId").value(suspendedUser.getId()));
    }

    @Test
    @DisplayName("관리자는 사용자 통계를 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getUserStatistics_Success() throws Exception {
        mockMvc.perform(get("/api/admin/user-management/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.activeUsers").exists())
                .andExpect(jsonPath("$.suspendedUsers").exists())
                .andExpect(jsonPath("$.withdrawnUsers").exists())
                .andExpect(jsonPath("$.adminUsers").exists())
                .andExpect(jsonPath("$.oauthUsers").exists());
    }

    @Test
    @DisplayName("일반 사용자는 관리자 API에 접근할 수 없다")
    @WithMockUser(roles = "USER")
    void accessDenied_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/user-management"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 404를 반환한다")
    @WithMockUser(roles = "ADMIN")
    void getUserDetail_NotFound() throws Exception {
        mockMvc.perform(get("/api/admin/user-management/nonexistent"))
                .andExpect(status().isNotFound());
    }

    private User createTestUser(String id, String name, String email, boolean isAdmin, UserStatus status) {
        User user = User.builder()
                .id(id)
                .pwd("password")
                .name(name)
                .nickname(name + "닉네임")
                .email(email)
                .isAdmin(isAdmin)
                .isActive(status == UserStatus.ACTIVE)
                .status(status)
                .build();
        return userRepository.save(user);
    }
} 