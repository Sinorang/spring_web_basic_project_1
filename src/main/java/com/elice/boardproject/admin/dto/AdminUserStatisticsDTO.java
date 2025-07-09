package com.elice.boardproject.admin.dto;

import com.elice.boardproject.acc.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 관리자용 사용자 통계 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserStatisticsDTO {
    private long totalUsers;
    private long activeUsers;
    private long suspendedUsers;
    private long withdrawnUsers;
    private long adminUsers;
    private long oauthUsers;
    private long recentJoinUsers7Days;
    private long recentJoinUsers30Days;
    private Map<UserStatus, Long> statusDistribution;

    public static AdminUserStatisticsDTO of(
            long totalUsers,
            long activeUsers,
            long suspendedUsers,
            long withdrawnUsers,
            long adminUsers,
            long oauthUsers,
            long recentJoinUsers7Days,
            long recentJoinUsers30Days,
            Map<UserStatus, Long> statusDistribution) {
        return AdminUserStatisticsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .suspendedUsers(suspendedUsers)
                .withdrawnUsers(withdrawnUsers)
                .adminUsers(adminUsers)
                .oauthUsers(oauthUsers)
                .recentJoinUsers7Days(recentJoinUsers7Days)
                .recentJoinUsers30Days(recentJoinUsers30Days)
                .statusDistribution(statusDistribution)
                .build();
    }
} 