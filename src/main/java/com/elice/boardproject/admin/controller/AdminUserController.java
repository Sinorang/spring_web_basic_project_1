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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * 사용자에게 관리자 권한 부여 (슈퍼 관리자만 가능)
     */
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<AdminUserDTO> grantAdminRole(
            @PathVariable Long userId,
            @Valid @RequestBody GrantAdminRoleRequest request) {
        Long currentUserId = getCurrentUserId();
        AdminUserDTO user = adminUserService.grantAdminRole(userId, request.getRoleName(), request.getGrantedBy(), currentUserId);
        return ResponseEntity.ok(user);
    }

    /**
     * 사용자의 관리자 권한 해제 (슈퍼 관리자만 가능)
     */
    @DeleteMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<AdminUserDTO> revokeAdminRole(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        AdminUserDTO user = adminUserService.revokeAdminRole(userId, currentUserId);
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

    /**
     * 현재 로그인한 사용자의 ID를 가져옵니다.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.elice.boardproject.acc.entity.UserDetailsImpl) {
            com.elice.boardproject.acc.entity.UserDetailsImpl userDetails = 
                (com.elice.boardproject.acc.entity.UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getUser().getIdx();
        }
        return null;
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