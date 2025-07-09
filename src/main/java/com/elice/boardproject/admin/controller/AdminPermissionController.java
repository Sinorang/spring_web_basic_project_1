package com.elice.boardproject.admin.controller;

import com.elice.boardproject.admin.dto.PermissionDTO;
import com.elice.boardproject.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPermissionController {

    private final PermissionService permissionService;

    /**
     * 모든 권한 조회
     */
    @GetMapping
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        List<PermissionDTO> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    /**
     * 권한명으로 권한 조회
     */
    @GetMapping("/{permissionName}")
    public ResponseEntity<PermissionDTO> getPermissionByPermissionName(@PathVariable String permissionName) {
        PermissionDTO permission = permissionService.getPermissionByPermissionName(permissionName);
        return ResponseEntity.ok(permission);
    }

    /**
     * 새로운 권한 생성
     */
    @PostMapping
    public ResponseEntity<PermissionDTO> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        PermissionDTO permission = permissionService.createPermission(
                request.getPermissionName(), 
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }

    /**
     * 권한 수정
     */
    @PutMapping("/{permissionId}")
    public ResponseEntity<PermissionDTO> updatePermission(
            @PathVariable Long permissionId,
            @Valid @RequestBody UpdatePermissionRequest request) {
        PermissionDTO permission = permissionService.updatePermission(
                permissionId, 
                request.getDescription()
        );
        return ResponseEntity.ok(permission);
    }

    /**
     * 권한 삭제
     */
    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long permissionId) {
        permissionService.deletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }

    // Request DTOs
    public static class CreatePermissionRequest {
        private String permissionName;
        private String description;

        // Getters and Setters
        public String getPermissionName() { return permissionName; }
        public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class UpdatePermissionRequest {
        private String description;

        // Getters and Setters
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
} 