package com.elice.boardproject.acc.entity;

/**
 * 사용자 상태를 관리하는 enum
 */
public enum UserStatus {
    ACTIVE("활성"),           // 활성 사용자 (정상)
    SUSPENDED("정지"),        // 정지된 사용자 (관리자 정지)
    WITHDRAWN("탈퇴");        // 탈퇴한 사용자 (자발적 탈퇴)

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 