package com.elice.boardproject.admin.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserStatus;
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
import java.util.Map;

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
     * 사용자가 슈퍼 관리자인지 확인합니다.
     */
    public boolean isSuperAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new PliException(ErrorCode.USER_NOT_FOUND, userId.toString()));

        if (!user.isAdmin() || user.getAdminRole() == null) {
            return false;
        }

        if (user.getAdminRole().getPermissions() == null) {
            return false;
        }

        return user.getAdminRole().getPermissions().stream()
                .anyMatch(permission -> "SUPER_ADMIN".equals(permission.getPermissionName()));
    }

    /**
     * 슈퍼 관리자 권한을 확인하고 권한 부여를 수행합니다.
     */
    @Transactional
    public AdminUserDTO grantAdminRole(Long targetUserId, String roleName, String grantedBy, Long superAdminUserId) {
        // 슈퍼 관리자 권한 확인
        if (!isSuperAdmin(superAdminUserId)) {
            throw new PliException(ErrorCode.INSUFFICIENT_PERMISSION, "슈퍼 관리자 권한이 필요합니다.");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new PliException(ErrorCode.USER_NOT_FOUND, targetUserId.toString()));

        AdminRole adminRole = adminRoleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new PliException(ErrorCode.ADMIN_ROLE_NOT_FOUND, roleName));

        targetUser.setAdmin(true);
        targetUser.setAdminRole(adminRole);
        targetUser.setAdminGrantedAt(LocalDateTime.now());
        targetUser.setAdminGrantedBy(grantedBy);

        User savedUser = userRepository.save(targetUser);
        return adminMapper.toAdminUserDTO(savedUser);
    }

    /**
     * 슈퍼 관리자 권한을 확인하고 권한 회수를 수행합니다.
     */
    @Transactional
    public AdminUserDTO revokeAdminRole(Long targetUserId, Long superAdminUserId) {
        // 슈퍼 관리자 권한 확인
        if (!isSuperAdmin(superAdminUserId)) {
            throw new PliException(ErrorCode.INSUFFICIENT_PERMISSION, "슈퍼 관리자 권한이 필요합니다.");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new PliException(ErrorCode.USER_NOT_FOUND, targetUserId.toString()));

        // 자기 자신의 권한을 회수하려는 경우 방지
        if (targetUserId.equals(superAdminUserId)) {
            throw new PliException(ErrorCode.INVALID_OPERATION, "자기 자신의 권한을 회수할 수 없습니다.");
        }

        targetUser.setAdmin(false);
        targetUser.setAdminRole(null);
        targetUser.setAdminGrantedAt(null);
        targetUser.setAdminGrantedBy(null);

        User savedUser = userRepository.save(targetUser);
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
        long activeUsers = userRepository.countByIsActiveTrue();
        long suspendedUsers = userRepository.countByStatus(UserStatus.SUSPENDED);
        long withdrawnUsers = userRepository.countByStatus(UserStatus.WITHDRAWN);
        long adminUsers = userRepository.countByIsAdminTrue();
        long oauthUsers = userRepository.countByOauthProviderIsNotNull();
        
        // 최근 가입자 수 (7일, 30일) - 간단한 구현
        long recentJoinUsers7Days = 0;  // TODO: 실제 구현 필요
        long recentJoinUsers30Days = 0; // TODO: 실제 구현 필요
        
        // 상태별 분포
        Map<UserStatus, Long> statusDistribution = Map.of(
            UserStatus.ACTIVE, activeUsers,
            UserStatus.SUSPENDED, suspendedUsers,
            UserStatus.WITHDRAWN, withdrawnUsers
        );
        
        return AdminUserStatisticsDTO.of(
            totalUsers,
            activeUsers,
            suspendedUsers,
            withdrawnUsers,
            adminUsers,
            oauthUsers,
            recentJoinUsers7Days,
            recentJoinUsers30Days,
            statusDistribution
        );
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