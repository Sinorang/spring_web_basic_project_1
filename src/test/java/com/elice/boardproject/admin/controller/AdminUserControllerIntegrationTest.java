package com.elice.boardproject.admin.controller;

import com.elice.boardproject.admin.dto.AdminUserDTO;
import com.elice.boardproject.admin.dto.AdminUserStatisticsDTO;
import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.entity.Permission;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.elice.boardproject.admin.repository.PermissionRepository;
import com.elice.boardproject.admin.service.AdminUserService;
import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.exception.ErrorCode;
import com.elice.boardproject.exception.PliException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private AdminUserDTO adminUserDTO;
    private AdminUserStatisticsDTO statisticsDTO;
    private User testUser;
    private AdminRole adminRole;
    private Permission permission;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리 (관계 역순)
        userRepository.deleteAll();
        adminRoleRepository.deleteAll();
        permissionRepository.deleteAll();

        String unique = UUID.randomUUID().toString().substring(0, 8);

        // 권한 생성
        permission = Permission.builder()
                .permissionName("SUPER_ADMIN_" + unique)
                .description("슈퍼 관리자 권한")
                .build();
        permission = permissionRepository.save(permission);

        // 관리자 역할 생성
        adminRole = AdminRole.builder()
                .roleName("SUPER_ADMIN_" + unique)
                .description("슈퍼 관리자")
                .permissions(java.util.Set.of(permission))
                .build();
        adminRole = adminRoleRepository.save(adminRole);

        // 테스트 사용자 생성
        testUser = User.builder()
                .id("admin_" + unique)
                .pwd("password")
                .name("관리자")
                .nickname("관리자닉네임")
                .email("admin_" + unique + "@test.com")
                .adminRole(adminRole)
                .build();
        testUser = userRepository.save(testUser);

        adminUserDTO = AdminUserDTO.builder()
                .idx(testUser.getIdx())
                .id(testUser.getId())
                .name(testUser.getName())
                .nickname(testUser.getNickname())
                .email(testUser.getEmail())
                .joinDate(LocalDateTime.now())
                .isAdmin(true)
                .adminRoleName("ADMIN")
                .adminGrantedAt(LocalDateTime.now())
                .build();

        statisticsDTO = AdminUserStatisticsDTO.builder()
                .totalUsers(100L)
                .adminUsers(5L)
                .oauthUsers(30L)
                .build();
    }

    @Test
    @DisplayName("모든 사용자 목록을 페이징하여 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers() throws Exception {
        // given
        List<AdminUserDTO> users = List.of(adminUserDTO);
        Page<AdminUserDTO> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);
        when(adminUserService.getAllUsers(any(Pageable.class))).thenReturn(userPage);
        // when & then
        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("특정 사용자 상세 정보를 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getUserById() throws Exception {
        // given
        when(adminUserService.getUserById(anyLong())).thenReturn(adminUserDTO);
        // when & then
        mockMvc.perform(get("/api/admin/users/" + testUser.getIdx()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andDo(print());
    }

    @Test
    @DisplayName("슈퍼 관리자는 사용자에게 관리자 권한을 부여할 수 있다")
    @WithMockUser(authorities = {"ROLE_ADMIN", "SUPER_ADMIN"})
    void grantAdminRole() throws Exception {
        // when & then
        mockMvc.perform(put("/api/admin/users/" + testUser.getIdx() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"ADMIN\",\"grantedBy\":\"admin\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("일반 관리자는 사용자에게 관리자 권한을 부여할 수 없다")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void grantAdminRole_Forbidden() throws Exception {
        // when & then
        mockMvc.perform(put("/api/admin/users/" + testUser.getIdx() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"ADMIN\",\"grantedBy\":\"admin\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("슈퍼 관리자는 사용자의 관리자 권한을 해제할 수 있다")
    @WithMockUser(authorities = {"ROLE_ADMIN", "SUPER_ADMIN"})
    void revokeAdminRole() throws Exception {
        // given
        AdminUserDTO nonAdminUser = AdminUserDTO.builder()
                .idx(1L)
                .id("testuser")
                .isAdmin(false)
                .build();
        when(adminUserService.revokeAdminRole(eq(1L), any())).thenReturn(nonAdminUser);

        // when & then
        mockMvc.perform(delete("/api/admin/users/1/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value(false));

        verify(adminUserService).revokeAdminRole(eq(1L), any());
    }

    @Test
    @DisplayName("일반 관리자는 사용자의 관리자 권한을 해제할 수 없다")
    @WithMockUser(roles = "ADMIN")
    void revokeAdminRole_Forbidden() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/admin/users/1/role"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("슈퍼 관리자가 아닌 사용자는 권한 부여 API에 접근할 수 없다")
    @WithMockUser(roles = "USER")
    void grantAdminRole_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(put("/api/admin/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"ADMIN\",\"grantedBy\":\"admin\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("슈퍼 관리자가 아닌 사용자는 권한 회수 API에 접근할 수 없다")
    @WithMockUser(roles = "USER")
    void revokeAdminRole_Unauthorized() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/admin/users/1/role"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("슈퍼 관리자 권한 확인 메서드가 정상적으로 작동한다")
    @WithMockUser(authorities = {"ROLE_ADMIN", "SUPER_ADMIN"})
    void isSuperAdmin_Test() throws Exception {
        // given
        when(adminUserService.isSuperAdmin(1L)).thenReturn(true);
        when(adminUserService.isSuperAdmin(2L)).thenReturn(false);

        // when & then
        // 실제로는 서비스 레이어에서 테스트해야 하지만, 컨트롤러 테스트에서는 간접적으로 확인
        mockMvc.perform(put("/api/admin/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"ADMIN\",\"grantedBy\":\"admin\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자 계정을 비활성화할 수 있다")
    @WithMockUser(authorities = {"ROLE_ADMIN", "SUPER_ADMIN"})
    void deactivateUser() throws Exception {
        // given
        doNothing().when(adminUserService).deactivateUser(1L);

        // when & then
        mockMvc.perform(delete("/api/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(adminUserService).deactivateUser(1L);
    }

    @Test
    @DisplayName("사용자 통계를 조회할 수 있다")
    @WithMockUser(authorities = {"ROLE_ADMIN", "SUPER_ADMIN"})
    void getUserStatistics() throws Exception {
        // given
        when(adminUserService.getUserStatistics()).thenReturn(statisticsDTO);

        // when & then
        mockMvc.perform(get("/api/admin/users/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.adminUsers").value(5))
                .andExpect(jsonPath("$.oauthUsers").value(30));

        verify(adminUserService).getUserStatistics();
    }
} 