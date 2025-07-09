package com.elice.boardproject.admin.controller;

import com.elice.boardproject.admin.dto.AdminUserDTO;
import com.elice.boardproject.admin.dto.AdminUserStatisticsDTO;
import com.elice.boardproject.admin.service.AdminUserService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    @Autowired
    private ObjectMapper objectMapper;

    private AdminUserDTO adminUserDTO;
    private AdminUserStatisticsDTO statisticsDTO;

    @BeforeEach
    void setUp() {
        adminUserDTO = AdminUserDTO.builder()
                .idx(1L)
                .id("testuser")
                .name("테스트")
                .nickname("테스트닉네임")
                .email("test@test.com")
                .joinDate(LocalDateTime.now())
                .isAdmin(true)
                .adminRoleName("ADMIN")
                .adminGrantedAt(LocalDateTime.now())
                .build();

        statisticsDTO = AdminUserStatisticsDTO.builder()
                .totalUsers(100L)
                .adminUsers(5L)
                .oauthUsers(30L)
                .regularUsers(70L)
                .build();
    }

    @Test
    @DisplayName("모든 사용자 목록을 페이징하여 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers() throws Exception {
        // given
        List<AdminUserDTO> users = Arrays.asList(adminUserDTO);
        Page<AdminUserDTO> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);
        when(adminUserService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // when & then
        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value("testuser"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(adminUserService).getAllUsers(any(Pageable.class));
    }

    @Test
    @DisplayName("특정 사용자 상세 정보를 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getUserById() throws Exception {
        // given
        when(adminUserService.getUserById(1L)).thenReturn(adminUserDTO);

        // when & then
        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("testuser"))
                .andExpect(jsonPath("$.name").value("테스트"))
                .andExpect(jsonPath("$.admin").value(true))
                .andDo(print());

        verify(adminUserService).getUserById(1L);
    }

    @Test
    @DisplayName("사용자에게 관리자 권한을 부여할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void grantAdminRole() throws Exception {
        // given
        when(adminUserService.grantAdminRole(eq(1L), eq("ADMIN"), eq("admin")))
                .thenReturn(adminUserDTO);

        // when & then
        mockMvc.perform(put("/api/admin/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"ADMIN\",\"grantedBy\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value(true));

        verify(adminUserService).grantAdminRole(1L, "ADMIN", "admin");
    }

    @Test
    @DisplayName("사용자의 관리자 권한을 해제할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void revokeAdminRole() throws Exception {
        // given
        AdminUserDTO nonAdminUser = AdminUserDTO.builder()
                .idx(1L)
                .id("testuser")
                .isAdmin(false)
                .build();
        when(adminUserService.revokeAdminRole(1L)).thenReturn(nonAdminUser);

        // when & then
        mockMvc.perform(delete("/api/admin/users/1/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value(false));

        verify(adminUserService).revokeAdminRole(1L);
    }

    @Test
    @DisplayName("사용자 계정을 비활성화할 수 있다")
    @WithMockUser(roles = "ADMIN")
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
    @WithMockUser(roles = "ADMIN")
    void getUserStatistics() throws Exception {
        // given
        when(adminUserService.getUserStatistics()).thenReturn(statisticsDTO);

        // when & then
        mockMvc.perform(get("/api/admin/users/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.adminUsers").value(5))
                .andExpect(jsonPath("$.oauthUsers").value(30))
                .andExpect(jsonPath("$.regularUsers").value(70));

        verify(adminUserService).getUserStatistics();
    }
} 