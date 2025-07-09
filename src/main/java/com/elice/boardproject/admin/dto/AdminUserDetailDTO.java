package com.elice.boardproject.admin.dto;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자용 사용자 상세 조회 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailDTO {
    private String userId;
    private String name;
    private String nickname;
    private String email;
    private boolean isActive;
    private UserStatus status;
    private String statusDescription;
    private boolean isAdmin;
    private String adminRoleName;
    private LocalDateTime joinDate;
    private LocalDateTime adminGrantedAt;
    private String adminGrantedBy;
    private String oauthProvider;
    private String oauthId;
    private String oauthEmail;
    
    // 통계 정보
    private long boardCount;
    private long postCount;
    private long commentCount;
    private long playlistCount;

    public static AdminUserDetailDTO from(User user) {
        return AdminUserDetailDTO.builder()
                .userId(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .isActive(user.isActive())
                .status(user.getStatus())
                .statusDescription(user.getStatus().getDescription())
                .isAdmin(user.isAdmin())
                .adminRoleName(user.getAdminRole() != null ? user.getAdminRole().getRoleName() : null)
                .joinDate(user.getJoinDate())
                .adminGrantedAt(user.getAdminGrantedAt())
                .adminGrantedBy(user.getAdminGrantedBy())
                .oauthProvider(user.getOauthProvider())
                .oauthId(user.getOauthId())
                .oauthEmail(user.getOauthEmail())
                .build();
    }
} 