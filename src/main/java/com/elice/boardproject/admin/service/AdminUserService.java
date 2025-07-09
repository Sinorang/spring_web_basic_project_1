package com.elice.boardproject.admin.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.admin.dto.AdminUserDTO;
import com.elice.boardproject.admin.dto.AdminUserStatisticsDTO;
import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.mapper.AdminMapper;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.elice.boardproject.exception.ErrorCode;
import com.elice.boardproject.exception.PliException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final AdminRoleRepository adminRoleRepository;
    private final AdminMapper adminMapper;

    /**
     * 모든 사용자를 페이징하여 조회합니다.
     */
    public Page<AdminUserDTO> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(adminMapper::toAdminUserDTO);
    }

    /**
     * 사용자 ID로 사용자를 조회합니다.
     */
    public AdminUserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PliException(ErrorCode.USER_NOT_FOUND, userId.toString()));
        return adminMapper.toAdminUserDTO(user);
    }

    /**
     * 사용자에게 관리자 권한을 부여합니다.
     */
    @Transactional
    public AdminUserDTO grantAdminRole(Long userId, String roleName, String grantedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PliException(ErrorCode.USER_NOT_FOUND, userId.toString()));

        AdminRole adminRole = adminRoleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new PliException(ErrorCode.ADMIN_ROLE_NOT_FOUND, roleName));

        user.setAdmin(true);
        user.setAdminRole(adminRole);
        user.setAdminGrantedAt(LocalDateTime.now());
        user.setAdminGrantedBy(grantedBy);

        User savedUser = userRepository.save(user);
        return adminMapper.toAdminUserDTO(savedUser);
    }

    /**
     * 사용자의 관리자 권한을 해제합니다.
     */
    @Transactional
    public AdminUserDTO revokeAdminRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PliException(ErrorCode.USER_NOT_FOUND, userId.toString()));

        user.setAdmin(false);
        user.setAdminRole(null);
        user.setAdminGrantedAt(null);
        user.setAdminGrantedBy(null);

        User savedUser = userRepository.save(user);
        return adminMapper.toAdminUserDTO(savedUser);
    }

    /**
     * 사용자 계정을 비활성화합니다.
     */
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PliException(ErrorCode.USER_NOT_FOUND, userId.toString()));

        user.setIsActive(false);
        userRepository.save(user);
    }

    /**
     * 사용자 통계를 조회합니다.
     */
    public AdminUserStatisticsDTO getUserStatistics() {
        long totalUsers = userRepository.count();
        long adminUsers = userRepository.countByIsAdminTrue();
        long oauthUsers = userRepository.countByOauthProviderIsNotNull();

        return AdminUserStatisticsDTO.builder()
                .totalUsers(totalUsers)
                .adminUsers(adminUsers)
                .oauthUsers(oauthUsers)
                .regularUsers(totalUsers - oauthUsers)
                .build();
    }

    /**
     * 사용자가 특정 권한을 가지고 있는지 확인합니다.
     */
    public boolean hasPermission(Long userId, String permissionName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PliException(ErrorCode.USER_NOT_FOUND, userId.toString()));

        if (!user.isAdmin() || user.getAdminRole() == null) {
            return false;
        }

        return user.getAdminRole().getPermissions().stream()
                .anyMatch(permission -> permission.getPermissionName().equals(permissionName));
    }
} 