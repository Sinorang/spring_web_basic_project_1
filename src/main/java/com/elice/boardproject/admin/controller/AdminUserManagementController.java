package com.elice.boardproject.admin.controller;

import com.elice.boardproject.admin.dto.AdminUserDetailDTO;
import com.elice.boardproject.admin.dto.AdminUserListDTO;
import com.elice.boardproject.admin.dto.AdminUserStatisticsDTO;
import com.elice.boardproject.admin.service.AdminUserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 관리자 전용 사용자 관리 API
 * 사용자 목록 조회, 상세 조회, 계정 정지/활성화, 통계 조회 기능 제공
 */
@RestController
@RequestMapping("/api/admin/user-management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    /**
     * 전체 사용자 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<AdminUserListDTO>> getAllUsers() {
        List<AdminUserListDTO> users = adminUserManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 특정 사용자 상세 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailDTO> getUserDetail(@PathVariable String userId) {
        try {
            AdminUserDetailDTO userDetail = adminUserManagementService.getUserDetail(userId);
            return ResponseEntity.ok(userDetail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 사용자 계정 정지
     */
    @PutMapping("/{userId}/suspend")
    public ResponseEntity<Map<String, Object>> suspendUser(@PathVariable String userId) {
        try {
            adminUserManagementService.suspendUser(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "사용자 계정이 정지되었습니다.",
                "userId", userId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "사용자 계정 정지 중 오류가 발생했습니다.",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 사용자 계정 활성화
     */
    @PutMapping("/{userId}/activate")
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable String userId) {
        try {
            adminUserManagementService.activateUser(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "사용자 계정이 활성화되었습니다.",
                "userId", userId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "사용자 계정 활성화 중 오류가 발생했습니다.",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 사용자 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<AdminUserStatisticsDTO> getUserStatistics() {
        AdminUserStatisticsDTO statistics = adminUserManagementService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }
} 