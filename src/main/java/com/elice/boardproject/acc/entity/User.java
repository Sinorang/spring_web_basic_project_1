package com.elice.boardproject.acc.entity;

import com.elice.boardproject.admin.entity.AdminRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@NoArgsConstructor
// @AllArgsConstructor 제거
// @Builder 어노테이션은 아래 커스텀 생성자에만 적용
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id // 기본키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDENTITY - 기본 값 생성을 db에 위임
    @Column(name = "user_idx")
    private Long idx;

    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = true)
    private String pwd;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(name = "join_date")
    @CreationTimestamp
    private LocalDateTime joinDate;

    // OAuth 관련 필드
    @Column(name = "oauth_provider")
    private String oauthProvider;

    @Column(name = "oauth_id")
    private String oauthId;

    @Column(name = "oauth_email")
    private String oauthEmail;

    // 관리자 관련 필드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_role_id")
    private AdminRole adminRole;

    @Column(name = "is_admin")
    private boolean isAdmin = false;

    @Column(name = "admin_granted_at")
    private LocalDateTime adminGrantedAt;

    @Column(name = "admin_granted_by")
    private String adminGrantedBy;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "withdraw_reason")
    private String reason; // 탈퇴 사유

    public User(String id, String pwd, String name, String nickname, String email) {
        this.id = id;
        this.pwd = pwd;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.isActive = true;
        this.status = UserStatus.ACTIVE;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    // @Builder에 reason 필드 추가
    @Builder
    public User(Long idx, String id, String pwd, String name, String nickname, String email, LocalDateTime joinDate, String oauthProvider, String oauthId, String oauthEmail, AdminRole adminRole, boolean isAdmin, LocalDateTime adminGrantedAt, String adminGrantedBy, boolean isActive, UserStatus status, String reason) {
        this.idx = idx;
        this.id = id;
        this.pwd = pwd;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.joinDate = joinDate;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.oauthEmail = oauthEmail;
        this.adminRole = adminRole;
        this.isAdmin = isAdmin;
        this.adminGrantedAt = adminGrantedAt;
        this.adminGrantedBy = adminGrantedBy;
        this.isActive = isActive;
        this.status = status;
        this.reason = reason;
    }
}