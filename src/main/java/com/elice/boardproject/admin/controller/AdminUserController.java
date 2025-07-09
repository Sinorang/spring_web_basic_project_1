package com.elice.boardproject.admin.controller;

import com.elice.boardproject.admin.dto.AdminUserDTO;
import com.elice.boardproject.admin.dto.AdminUserStatisticsDTO;
import com.elice.boardproject.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 모든 사용자 목록을 페이징하여 조회
     */
    @GetMapping
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(Pageable pageable) {
        Page<AdminUserDTO> users = adminUserService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * 특정 사용자 상세 정보 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long userId) {
        AdminUserDTO user = adminUserService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자에게 관리자 권한 부여
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<AdminUserDTO> grantAdminRole(
            @PathVariable Long userId,
            @Valid @RequestBody GrantAdminRoleRequest request) {
        AdminUserDTO user = adminUserService.grantAdminRole(userId, request.getRoleName(), request.getGrantedBy());
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자의 관리자 권한 해제
     */
    @DeleteMapping("/{userId}/role")
    public ResponseEntity<AdminUserDTO> revokeAdminRole(@PathVariable Long userId) {
        AdminUserDTO user = adminUserService.revokeAdminRole(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자 계정 비활성화
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        adminUserService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 사용자 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<AdminUserStatisticsDTO> getUserStatistics() {
        AdminUserStatisticsDTO statistics = adminUserService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }

    // Request DTO
    public static class GrantAdminRoleRequest {
        private String roleName;
        private String grantedBy;

        // Getters and Setters
        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        public String getGrantedBy() {
            return grantedBy;
        }

        public void setGrantedBy(String grantedBy) {
            this.grantedBy = grantedBy;
        }
    }
} 