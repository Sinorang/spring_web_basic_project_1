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
@AllArgsConstructor
@Builder
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
}