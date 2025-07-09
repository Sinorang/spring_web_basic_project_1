package com.elice.boardproject.admin.service;

import com.elice.boardproject.admin.dto.PermissionDTO;
import com.elice.boardproject.admin.entity.Permission;
import com.elice.boardproject.admin.mapper.AdminMapper;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private PermissionService permissionService;

    private Permission permission;
    private PermissionDTO permissionDTO;

    @BeforeEach
    void setUp() {
        permission = Permission.builder()
                .id(1L)
                .permissionName("USER_MANAGE")
                .description("사용자 관리 권한")
                .build();

        permissionDTO = PermissionDTO.builder()
                .id(1L)
                .permissionName("USER_MANAGE")
                .description("사용자 관리 권한")
                .build();
    }

    @Test
    @DisplayName("모든 권한을 조회할 수 있다")
    void getAllPermissions() {
        // given
        List<Permission> permissions = Arrays.asList(permission);
        when(permissionRepository.findAll()).thenReturn(permissions);
        when(adminMapper.toPermissionDTO(any(Permission.class))).thenReturn(permissionDTO);

        // when
        List<PermissionDTO> result = permissionService.getAllPermissions();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPermissionName()).isEqualTo("USER_MANAGE");
        verify(permissionRepository).findAll();
        verify(adminMapper).toPermissionDTO(permission);
    }

    @Test
    @DisplayName("권한명으로 권한을 조회할 수 있다")
    void getPermissionByPermissionName() {
        // given
        when(permissionRepository.findByPermissionName("USER_MANAGE")).thenReturn(Optional.of(permission));
        when(adminMapper.toPermissionDTO(permission)).thenReturn(permissionDTO);

        // when
        PermissionDTO result = permissionService.getPermissionByPermissionName("USER_MANAGE");

        // then
        assertThat(result.getPermissionName()).isEqualTo("USER_MANAGE");
        verify(permissionRepository).findByPermissionName("USER_MANAGE");
    }

    @Test
    @DisplayName("존재하지 않는 권한명으로 조회 시 예외가 발생한다")
    void getPermissionByPermissionName_NotFound() {
        // given
        when(permissionRepository.findByPermissionName("NOT_EXIST")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> permissionService.getPermissionByPermissionName("NOT_EXIST"))
                .isInstanceOf(PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERMISSION_NOT_FOUND);
    }

    @Test
    @DisplayName("새로운 권한을 생성할 수 있다")
    void createPermission() {
        // given
        when(permissionRepository.existsByPermissionName("NEW_PERMISSION")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(permission);
        when(adminMapper.toPermissionDTO(permission)).thenReturn(permissionDTO);

        // when
        PermissionDTO result = permissionService.createPermission("NEW_PERMISSION", "새 권한");

        // then
        assertThat(result.getPermissionName()).isEqualTo("USER_MANAGE");
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("이미 존재하는 권한명으로 생성 시 예외가 발생한다")
    void createPermission_AlreadyExists() {
        // given
        when(permissionRepository.existsByPermissionName("USER_MANAGE")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> permissionService.createPermission("USER_MANAGE", "사용자 관리 권한"))
                .isInstanceOf(PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PERMISSION_ALREADY_EXISTS);
    }
} 