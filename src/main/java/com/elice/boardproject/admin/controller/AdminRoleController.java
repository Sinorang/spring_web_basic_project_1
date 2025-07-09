package com.elice.boardproject.admin.controller;

import com.elice.boardproject.admin.dto.AdminRoleDTO;
import com.elice.boardproject.admin.service.AdminRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    /**
     * 모든 관리자 역할 조회
     */
    @GetMapping
    public ResponseEntity<List<AdminRoleDTO>> getAllAdminRoles() {
        List<AdminRoleDTO> roles = adminRoleService.getAllAdminRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * 역할명으로 관리자 역할 조회
     */
    @GetMapping("/{roleName}")
    public ResponseEntity<AdminRoleDTO> getAdminRoleByRoleName(@PathVariable String roleName) {
        AdminRoleDTO role = adminRoleService.getAdminRoleByRoleName(roleName);
        return ResponseEntity.ok(role);
    }

    /**
     * 새로운 관리자 역할 생성
     */
    @PostMapping
    public ResponseEntity<AdminRoleDTO> createAdminRole(@Valid @RequestBody CreateAdminRoleRequest request) {
        AdminRoleDTO role = adminRoleService.createAdminRole(
                request.getRoleName(), 
                request.getDescription(), 
                request.getPermissionNames()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    /**
     * 관리자 역할 수정
     */
    @PutMapping("/{roleId}")
    public ResponseEntity<AdminRoleDTO> updateAdminRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateAdminRoleRequest request) {
        AdminRoleDTO role = adminRoleService.updateAdminRole(
                roleId, 
                request.getDescription(), 
                request.getPermissionNames()
        );
        return ResponseEntity.ok(role);
    }

    /**
     * 관리자 역할 삭제
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteAdminRole(@PathVariable Long roleId) {
        adminRoleService.deleteAdminRole(roleId);
        return ResponseEntity.noContent().build();
    }

    // Request DTOs
    public static class CreateAdminRoleRequest {
        private String roleName;
        private String description;
        private Set<String> permissionNames;

        // Getters and Setters
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Set<String> getPermissionNames() { return permissionNames; }
        public void setPermissionNames(Set<String> permissionNames) { this.permissionNames = permissionNames; }
    }

    public static class UpdateAdminRoleRequest {
        private String description;
        private Set<String> permissionNames;

        // Getters and Setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Set<String> getPermissionNames() { return permissionNames; }
        public void setPermissionNames(Set<String> permissionNames) { this.permissionNames = permissionNames; }
    }
} 