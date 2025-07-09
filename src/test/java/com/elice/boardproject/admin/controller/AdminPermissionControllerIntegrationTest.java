package com.elice.boardproject.admin.controller;

import com.elice.boardproject.admin.dto.PermissionDTO;
import com.elice.boardproject.admin.service.PermissionService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPermissionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermissionService permissionService;

    @Autowired
    private ObjectMapper objectMapper;

    private PermissionDTO permissionDTO;

    @BeforeEach
    void setUp() {
        permissionDTO = PermissionDTO.builder()
                .id(1L)
                .permissionName("USER_MANAGE")
                .description("사용자 관리 권한")
                .build();
    }

    @Test
    @DisplayName("모든 권한 목록을 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getAllPermissions() throws Exception {
        // given
        List<PermissionDTO> permissions = Arrays.asList(permissionDTO);
        when(permissionService.getAllPermissions()).thenReturn(permissions);

        // when & then
        mockMvc.perform(get("/api/admin/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].permissionName").value("USER_MANAGE"))
                .andExpect(jsonPath("$[0].description").value("사용자 관리 권한"));

        verify(permissionService).getAllPermissions();
    }

    @Test
    @DisplayName("권한명으로 특정 권한을 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getPermissionByName() throws Exception {
        // given
        when(permissionService.getPermissionByPermissionName("USER_MANAGE")).thenReturn(permissionDTO);

        // when & then
        mockMvc.perform(get("/api/admin/permissions/USER_MANAGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionName").value("USER_MANAGE"))
                .andExpect(jsonPath("$.description").value("사용자 관리 권한"));

        verify(permissionService).getPermissionByPermissionName("USER_MANAGE");
    }

    @Test
    @DisplayName("새로운 권한을 생성할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void createPermission() throws Exception {
        // given
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setPermissionName("NEW_PERMISSION");
        request.setDescription("새로운 권한");

        when(permissionService.createPermission(eq("NEW_PERMISSION"), eq("새로운 권한")))
                .thenReturn(permissionDTO);

        // when & then
        mockMvc.perform(post("/api/admin/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.permissionName").value("USER_MANAGE"));

        verify(permissionService).createPermission("NEW_PERMISSION", "새로운 권한");
    }

    @Test
    @DisplayName("권한을 수정할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void updatePermission() throws Exception {
        // given
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        request.setDescription("수정된 권한 설명");

        when(permissionService.updatePermission(eq(1L), eq("수정된 권한 설명")))
                .thenReturn(permissionDTO);

        // when & then
        mockMvc.perform(put("/api/admin/permissions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionName").value("USER_MANAGE"));

        verify(permissionService).updatePermission(1L, "수정된 권한 설명");
    }

    @Test
    @DisplayName("권한을 삭제할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void deletePermission() throws Exception {
        // given
        doNothing().when(permissionService).deletePermission(1L);

        // when & then
        mockMvc.perform(delete("/api/admin/permissions/1"))
                .andExpect(status().isNoContent());

        verify(permissionService).deletePermission(1L);
    }

    @Test
    @DisplayName("존재하지 않는 권한 조회 시 404 에러를 반환한다")
    @WithMockUser(roles = "ADMIN")
    void getPermissionByName_NotFound() throws Exception {
        // given
        when(permissionService.getPermissionByPermissionName("NOT_EXIST"))
                .thenThrow(new PliException(ErrorCode.PERMISSION_NOT_FOUND, "NOT_EXIST"));

        // when & then
        mockMvc.perform(get("/api/admin/permissions/NOT_EXIST"))
                .andExpect(status().isNotFound());

        verify(permissionService).getPermissionByPermissionName("NOT_EXIST");
    }

    // Request DTOs
    public static class CreatePermissionRequest {
        private String permissionName;
        private String description;

        // Getters and Setters
        public String getPermissionName() { return permissionName; }
        public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class UpdatePermissionRequest {
        private String description;

        // Getters and Setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
} 