package com.elice.boardproject.admin.service;

import com.elice.boardproject.admin.dto.AdminRoleDTO;
import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.entity.Permission;
import com.elice.boardproject.admin.mapper.AdminMapper;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.elice.boardproject.admin.repository.PermissionRepository;
import com.elice.boardproject.exception.ErrorCode;
import com.elice.boardproject.exception.PliException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRoleService {

    private final AdminRoleRepository adminRoleRepository;
    private final PermissionRepository permissionRepository;
    private final AdminMapper adminMapper;

    /**
     * 모든 관리자 역할을 조회합니다.
     */
    public List<AdminRoleDTO> getAllAdminRoles() {
        List<AdminRole> adminRoles = adminRoleRepository.findAll();
        return adminRoles.stream()
                .map(adminMapper::toAdminRoleDTO)
                .collect(Collectors.toList());
    }

    /**
     * 역할명으로 관리자 역할을 조회합니다.
     */
    public AdminRoleDTO getAdminRoleByRoleName(String roleName) {
        AdminRole adminRole = adminRoleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new PliException(ErrorCode.ADMIN_ROLE_NOT_FOUND, roleName));
        return adminMapper.toAdminRoleDTO(adminRole);
    }

    /**
     * 새로운 관리자 역할을 생성합니다.
     */
    @Transactional
    public AdminRoleDTO createAdminRole(String roleName, String description, Set<String> permissionNames) {
        // 역할명 중복 검사
        if (adminRoleRepository.existsByRoleName(roleName)) {
            throw new PliException(ErrorCode.ADMIN_ROLE_ALREADY_EXISTS, roleName);
        }

        // 권한 조회
        Set<Permission> permissions = permissionNames.stream()
                .map(permissionName -> permissionRepository.findByPermissionName(permissionName)
                        .orElseThrow(() -> new PliException(ErrorCode.PERMISSION_NOT_FOUND, permissionName)))
                .collect(Collectors.toSet());

        // AdminRole 생성
        AdminRole adminRole = AdminRole.builder()
                .roleName(roleName)
                .description(description)
                .permissions(permissions)
                .build();

        AdminRole savedAdminRole = adminRoleRepository.save(adminRole);
        return adminMapper.toAdminRoleDTO(savedAdminRole);
    }

    /**
     * 관리자 역할을 삭제합니다.
     */
    @Transactional
    public void deleteAdminRole(Long roleId) {
        AdminRole adminRole = adminRoleRepository.findById(roleId)
                .orElseThrow(() -> new PliException(ErrorCode.ADMIN_ROLE_NOT_FOUND, roleId.toString()));
        adminRoleRepository.delete(adminRole);
    }

    /**
     * 관리자 역할을 수정합니다.
     */
    @Transactional
    public AdminRoleDTO updateAdminRole(Long roleId, String description, Set<String> permissionNames) {
        AdminRole adminRole = adminRoleRepository.findById(roleId)
                .orElseThrow(() -> new PliException(ErrorCode.ADMIN_ROLE_NOT_FOUND, roleId.toString()));

        // 권한 조회
        Set<Permission> permissions = permissionNames.stream()
                .map(permissionName -> permissionRepository.findByPermissionName(permissionName)
                        .orElseThrow(() -> new PliException(ErrorCode.PERMISSION_NOT_FOUND, permissionName)))
                .collect(Collectors.toSet());

        adminRole.setDescription(description);
        adminRole.setPermissions(permissions);

        return adminMapper.toAdminRoleDTO(adminRole);
    }
} 