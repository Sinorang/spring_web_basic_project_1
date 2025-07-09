package com.elice.boardproject.admin.controller;

import com.elice.boardproject.admin.dto.AdminRoleDTO;
import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.entity.Permission;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.elice.boardproject.admin.repository.PermissionRepository;
import com.elice.boardproject.admin.service.AdminRoleService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminRoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminRoleService adminRoleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRoleRepository adminRoleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private AdminRoleDTO adminRoleDTO;
    private User testUser;
    private AdminRole adminRole;
    private Permission permission;
    private String unique;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리 (관계 역순)
        userRepository.deleteAll();
        adminRoleRepository.deleteAll();
        permissionRepository.deleteAll();

        unique = UUID.randomUUID().toString().substring(0, 8);

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
                .permissions(Set.of(permission))
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

        adminRoleDTO = AdminRoleDTO.builder()
                .id(1L)
                .roleName("ADMIN")
                .description("일반 관리자")
                .permissionNames(Set.of("USER_MANAGE", "BOARD_MANAGE"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("모든 관리자 역할을 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getAllAdminRoles() throws Exception {
        // given
        List<AdminRoleDTO> roles = Arrays.asList(adminRoleDTO);
        when(adminRoleService.getAllAdminRoles()).thenReturn(roles);

        // when & then
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$[0].description").value("일반 관리자"));

        verify(adminRoleService).getAllAdminRoles();
    }

    @Test
    @DisplayName("역할명으로 관리자 역할을 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getAdminRoleByRoleName() throws Exception {
        // given
        when(adminRoleService.getAdminRoleByRoleName("ADMIN")).thenReturn(adminRoleDTO);

        // when & then
        mockMvc.perform(get("/api/admin/roles/ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("ADMIN"))
                .andExpect(jsonPath("$.description").value("일반 관리자"));

        verify(adminRoleService).getAdminRoleByRoleName("ADMIN");
    }

    @Test
    @DisplayName("새로운 관리자 역할을 생성할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void createAdminRole() throws Exception {
        // given
        CreateAdminRoleRequest request = new CreateAdminRoleRequest();
        request.setRoleName("NEW_ADMIN");
        request.setDescription("새 관리자");
        request.setPermissionNames(Set.of("USER_MANAGE"));

        when(adminRoleService.createAdminRole(eq("NEW_ADMIN"), eq("새 관리자"), any(Set.class)))
                .thenReturn(adminRoleDTO);

        // when & then
        mockMvc.perform(post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleName").value("ADMIN"));

        verify(adminRoleService).createAdminRole("NEW_ADMIN", "새 관리자", Set.of("USER_MANAGE"));
    }

    @Test
    @DisplayName("관리자 역할을 수정할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void updateAdminRole() throws Exception {
        // given
        UpdateAdminRoleRequest request = new UpdateAdminRoleRequest();
        request.setDescription("수정된 관리자");
        request.setPermissionNames(Set.of("USER_MANAGE", "PLAYLIST_MANAGE"));

        when(adminRoleService.updateAdminRole(eq(1L), eq("수정된 관리자"), any(Set.class)))
                .thenReturn(adminRoleDTO);

        // when & then
        mockMvc.perform(put("/api/admin/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("ADMIN"));

        verify(adminRoleService).updateAdminRole(1L, "수정된 관리자", Set.of("USER_MANAGE", "PLAYLIST_MANAGE"));
    }

    @Test
    @DisplayName("관리자 역할을 삭제할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void deleteAdminRole() throws Exception {
        // given
        doNothing().when(adminRoleService).deleteAdminRole(1L);

        // when & then
        mockMvc.perform(delete("/api/admin/roles/1"))
                .andExpect(status().isNoContent());

        verify(adminRoleService).deleteAdminRole(1L);
    }

    @Test
    @DisplayName("존재하지 않는 역할 조회 시 404 에러를 반환한다")
    @WithMockUser(roles = "ADMIN")
    void getAdminRoleByRoleName_NotFound() throws Exception {
        // given
        when(adminRoleService.getAdminRoleByRoleName("NOT_EXIST"))
                .thenThrow(new PliException(ErrorCode.ADMIN_ROLE_NOT_FOUND, "NOT_EXIST"));

        // when & then
        mockMvc.perform(get("/api/admin/roles/NOT_EXIST"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(10000));

        verify(adminRoleService).getAdminRoleByRoleName("NOT_EXIST");
    }

    @Test
    @DisplayName("관리자 권한이 없는 사용자는 접근할 수 없다")
    @WithMockUser(roles = "USER")
    void accessDenied_WithoutAdminRole() throws Exception {
        // when & then
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isForbidden());
    }

    // Request DTOs
    public static class CreateAdminRoleRequest {
        private String roleName;
        private String description;
        private Set<String> permissionNames;

        // Getters and Setters
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Set<String> getPermissionNames() { return permissionNames; }
        public void setPermissionNames(Set<String> permissionNames) { this.permissionNames = permissionNames; }
    }

    public static class UpdateAdminRoleRequest {
        private String description;
        private Set<String> permissionNames;

        // Getters and Setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Set<String> getPermissionNames() { return permissionNames; }
        public void setPermissionNames(Set<String> permissionNames) { this.permissionNames = permissionNames; }
    }
} 