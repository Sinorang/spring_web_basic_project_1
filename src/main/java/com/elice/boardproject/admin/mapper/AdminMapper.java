package com.elice.boardproject.admin.mapper;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.admin.dto.AdminRoleDTO;
import com.elice.boardproject.admin.dto.AdminUserDTO;
import com.elice.boardproject.admin.dto.PermissionDTO;
import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.entity.Permission;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AdminMapper {

    public AdminRoleDTO toAdminRoleDTO(AdminRole adminRole) {
        if (adminRole == null) {
            return null;
        }

        return AdminRoleDTO.builder()
                .id(adminRole.getId())
                .roleName(adminRole.getRoleName())
                .description(adminRole.getDescription())
                .permissionNames(adminRole.getPermissions().stream()
                        .map(Permission::getPermissionName)
                        .collect(Collectors.toSet()))
                .createdAt(adminRole.getCreatedAt())
                .build();
    }

    public PermissionDTO toPermissionDTO(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionDTO.builder()
                .id(permission.getId())
                .permissionName(permission.getPermissionName())
                .description(permission.getDescription())
                .build();
    }

    public AdminUserDTO toAdminUserDTO(User user) {
        if (user == null) {
            return null;
        }

        return AdminUserDTO.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .joinDate(user.getJoinDate())
                .oauthProvider(user.getOauthProvider())
                .oauthId(user.getOauthId())
                .oauthEmail(user.getOauthEmail())
                .isAdmin(user.isAdmin())
                .adminRoleName(user.getAdminRole() != null ? user.getAdminRole().getRoleName() : null)
                .adminGrantedAt(user.getAdminGrantedAt())
                .adminGrantedBy(user.getAdminGrantedBy())
                .build();
    }
} 