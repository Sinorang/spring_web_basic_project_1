package com.elice.boardproject.admin.dto;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 관리자용 사용자 목록 조회 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListDTO {
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
    private String oauthProvider;

    public static AdminUserListDTO from(User user) {
        return AdminUserListDTO.builder()
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
                .oauthProvider(user.getOauthProvider())
                .build();
    }
} 