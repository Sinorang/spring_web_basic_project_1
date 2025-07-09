package com.elice.boardproject.admin.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.admin.dto.AdminUserDTO;
import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.mapper.AdminMapper;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.elice.boardproject.exception.ErrorCode;
import com.elice.boardproject.exception.PliException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminRoleRepository adminRoleRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private AdminUserService adminUserService;

    private User user;
    private AdminUserDTO adminUserDTO;
    private AdminRole adminRole;

    @BeforeEach
    void setUp() {
        adminRole = AdminRole.builder()
                .id(1L)
                .roleName("ADMIN")
                .description("일반 관리자")
                .build();

        user = User.builder()
                .idx(1L)
                .id("testuser")
                .name("테스트")
                .nickname("테스트닉네임")
                .email("test@test.com")
                .joinDate(LocalDateTime.now())
                .isAdmin(true)
                .adminRole(adminRole)
                .adminGrantedAt(LocalDateTime.now())
                .build();

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
    }

    @Test
    @DisplayName("모든 사용자를 페이징하여 조회할 수 있다")
    void getAllUsers() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = Arrays.asList(user);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(adminMapper.toAdminUserDTO(any(User.class))).thenReturn(adminUserDTO);

        // when
        Page<AdminUserDTO> result = adminUserService.getAllUsers(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("testuser");
        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("사용자 ID로 사용자를 조회할 수 있다")
    void getUserById() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(adminMapper.toAdminUserDTO(user)).thenReturn(adminUserDTO);

        // when
        AdminUserDTO result = adminUserService.getUserById(1L);

        // then
        assertThat(result.getId()).isEqualTo("testuser");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회 시 예외가 발생한다")
    void getUserById_NotFound() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminUserService.getUserById(999L))
                .isInstanceOf(PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자에게 관리자 권한을 부여할 수 있다")
    void grantAdminRole() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(adminRoleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(adminMapper.toAdminUserDTO(user)).thenReturn(adminUserDTO);

        // when
        AdminUserDTO result = adminUserService.grantAdminRole(1L, "ADMIN", "admin");

        // then
        assertThat(result.isAdmin()).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자에게 권한 부여 시 예외가 발생한다")
    void grantAdminRole_UserNotFound() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminUserService.grantAdminRole(999L, "ADMIN", "admin"))
                .isInstanceOf(PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 역할로 권한 부여 시 예외가 발생한다")
    void grantAdminRole_RoleNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(adminRoleRepository.findByRoleName("INVALID_ROLE")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminUserService.grantAdminRole(1L, "INVALID_ROLE", "admin"))
                .isInstanceOf(PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_ROLE_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자의 관리자 권한을 해제할 수 있다")
    void revokeAdminRole() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(adminMapper.toAdminUserDTO(user)).thenReturn(adminUserDTO);

        // when
        AdminUserDTO result = adminUserService.revokeAdminRole(1L);

        // then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 계정을 비활성화할 수 있다")
    void deactivateUser() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        adminUserService.deactivateUser(1L);

        // then
        verify(userRepository).save(any(User.class));
    }
} 