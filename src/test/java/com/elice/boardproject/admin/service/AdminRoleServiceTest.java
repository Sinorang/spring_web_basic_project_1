package com.elice.boardproject.admin.service;

import com.elice.boardproject.admin.dto.AdminRoleDTO;
import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.entity.Permission;
import com.elice.boardproject.admin.mapper.AdminMapper;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.elice.boardproject.admin.repository.PermissionRepository;
import com.elice.boardproject.exception.ErrorCode;
import com.elice.boardproject.exception.PliException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminRoleServiceTest {

    @Mock
    private AdminRoleRepository adminRoleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private AdminRoleService adminRoleService;

    private AdminRole adminRole;
    private AdminRoleDTO adminRoleDTO;
    private Permission permission;

    @BeforeEach
    void setUp() {
        permission = Permission.builder()
                .id(1L)
                .permissionName("USER_MANAGE")
                .description("사용자 관리 권한")
                .build();

        adminRole = AdminRole.builder()
                .id(1L)
                .roleName("ADMIN")
                .description("일반 관리자")
                .permissions(Set.of(permission))
                .createdAt(LocalDateTime.now())
                .build();

        adminRoleDTO = AdminRoleDTO.builder()
                .id(1L)
                .roleName("ADMIN")
                .description("일반 관리자")
                .permissionNames(Set.of("USER_MANAGE"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("모든 관리자 역할을 조회할 수 있다")
    void getAllAdminRoles() {
        // given
        List<AdminRole> adminRoles = Arrays.asList(adminRole);
        when(adminRoleRepository.findAll()).thenReturn(adminRoles);
        when(adminMapper.toAdminRoleDTO(any(AdminRole.class))).thenReturn(adminRoleDTO);

        // when
        List<AdminRoleDTO> result = adminRoleService.getAllAdminRoles();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoleName()).isEqualTo("ADMIN");
        verify(adminRoleRepository).findAll();
        verify(adminMapper).toAdminRoleDTO(adminRole);
    }

    @Test
    @DisplayName("역할명으로 관리자 역할을 조회할 수 있다")
    void getAdminRoleByRoleName() {
        // given
        when(adminRoleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(adminMapper.toAdminRoleDTO(adminRole)).thenReturn(adminRoleDTO);

        // when
        AdminRoleDTO result = adminRoleService.getAdminRoleByRoleName("ADMIN");

        // then
        assertThat(result.getRoleName()).isEqualTo("ADMIN");
        verify(adminRoleRepository).findByRoleName("ADMIN");
    }

    @Test
    @DisplayName("존재하지 않는 역할명으로 조회 시 예외가 발생한다")
    void getAdminRoleByRoleName_NotFound() {
        // given
        when(adminRoleRepository.findByRoleName("NOT_EXIST")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminRoleService.getAdminRoleByRoleName("NOT_EXIST"))
                .isInstanceOf(PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_ROLE_NOT_FOUND);
    }

    @Test
    @DisplayName("새로운 관리자 역할을 생성할 수 있다")
    void createAdminRole() {
        // given
        when(adminRoleRepository.existsByRoleName("NEW_ADMIN")).thenReturn(false);
        when(permissionRepository.findByPermissionName("USER_MANAGE")).thenReturn(Optional.of(permission));
        when(adminRoleRepository.save(any(AdminRole.class))).thenReturn(adminRole);
        when(adminMapper.toAdminRoleDTO(adminRole)).thenReturn(adminRoleDTO);

        // when
        AdminRoleDTO result = adminRoleService.createAdminRole("NEW_ADMIN", "새 관리자", Set.of("USER_MANAGE"));

        // then
        assertThat(result.getRoleName()).isEqualTo("ADMIN");
        verify(adminRoleRepository).save(any(AdminRole.class));
    }

    @Test
    @DisplayName("이미 존재하는 역할명으로 생성 시 예외가 발생한다")
    void createAdminRole_AlreadyExists() {
        // given
        when(adminRoleRepository.existsByRoleName("ADMIN")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> adminRoleService.createAdminRole("ADMIN", "관리자", Set.of("USER_MANAGE")))
                .isInstanceOf(PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_ROLE_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("존재하지 않는 권한으로 역할 생성 시 예외가 발생한다")
    void createAdminRole_PermissionNotFound() {
        // given
        when(adminRoleRepository.existsByRoleName("NEW_ADMIN")).thenReturn(false);
        when(permissionRepository.findByPermissionName("INVALID_PERMISSION")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminRoleService.createAdminRole("NEW_ADMIN", "새 관리자", Set.of("INVALID_PERMISSION")))
                .isInstanceOf(PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERMISSION_NOT_FOUND);
    }
} 