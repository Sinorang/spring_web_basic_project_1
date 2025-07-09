package com.elice.boardproject.admin.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserStatus;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.admin.dto.AdminUserDetailDTO;
import com.elice.boardproject.admin.dto.AdminUserListDTO;
import com.elice.boardproject.admin.dto.AdminUserStatisticsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자용 사용자 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserManagementService {

    private final UserRepository userRepository;

    /**
     * 전체 사용자 목록 조회
     */
    public List<AdminUserListDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(AdminUserListDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자 상세 조회
     */
    public AdminUserDetailDTO getUserDetail(String userId) {
        User user = userRepository.findByIdForAdmin(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
        }
        return AdminUserDetailDTO.from(user);
    }

    /**
     * 사용자 계정 정지
     */
    @Transactional
    public void suspendUser(String userId) {
        User user = userRepository.findByIdForAdmin(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
        }
        
        // status 변경 시 isActive 자동 동기화
        user.setStatus(UserStatus.SUSPENDED);
        user.setIsActive(false);
        // save() 호출 없이 변경사항이 자동으로 반영됨 (JPA 영속성 컨텍스트)
    }

    /**
     * 사용자 계정 활성화
     */
    @Transactional
    public void activateUser(String userId) {
        User user = userRepository.findByIdForAdmin(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
        }
        
        // status 변경 시 isActive 자동 동기화
        user.setStatus(UserStatus.ACTIVE);
        user.setIsActive(true);
        // save() 호출 없이 변경사항이 자동으로 반영됨 (JPA 영속성 컨텍스트)
    }

    /**
     * 사용자 통계 조회
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
} 