package com.elice.boardproject.admin.service;

import com.elice.boardproject.admin.dto.PermissionDTO;
import com.elice.boardproject.admin.entity.Permission;
import com.elice.boardproject.admin.mapper.AdminMapper;
import com.elice.boardproject.admin.repository.PermissionRepository;
import com.elice.boardproject.exception.ErrorCode;
import com.elice.boardproject.exception.PliException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final AdminMapper adminMapper;

    /**
     * 모든 권한을 조회합니다.
     */
    public List<PermissionDTO> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(adminMapper::toPermissionDTO)
                .collect(Collectors.toList());
    }

    /**
     * 권한명으로 권한을 조회합니다.
     */
    public PermissionDTO getPermissionByPermissionName(String permissionName) {
        Permission permission = permissionRepository.findByPermissionName(permissionName)
                .orElseThrow(() -> new PliException(ErrorCode.PERMISSION_NOT_FOUND, permissionName));
        return adminMapper.toPermissionDTO(permission);
    }

    /**
     * 새로운 권한을 생성합니다.
     */
    @Transactional
    public PermissionDTO createPermission(String permissionName, String description) {
        // 권한명 중복 검사
        if (permissionRepository.existsByPermissionName(permissionName)) {
            throw new PliException(ErrorCode.PERMISSION_ALREADY_EXISTS, permissionName);
        }

        // Permission 생성
        Permission permission = Permission.builder()
                .permissionName(permissionName)
                .description(description)
                .build();

        Permission savedPermission = permissionRepository.save(permission);
        return adminMapper.toPermissionDTO(savedPermission);
    }

    /**
     * 권한을 삭제합니다.
     */
    @Transactional
    public void deletePermission(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PliException(ErrorCode.PERMISSION_NOT_FOUND, permissionId.toString()));
        permissionRepository.delete(permission);
    }

    /**
     * 권한을 수정합니다.
     */
    @Transactional
    public PermissionDTO updatePermission(Long permissionId, String description) {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PliException(ErrorCode.PERMISSION_NOT_FOUND, permissionId.toString()));

        permission.setDescription(description);
        return adminMapper.toPermissionDTO(permission);
    }
} 