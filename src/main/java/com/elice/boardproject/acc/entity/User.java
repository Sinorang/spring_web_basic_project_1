package com.elice.boardproject.acc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
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

    @Column(nullable = false)
    private String pwd;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String email;

    @Column(name = "join_date")
    @CreationTimestamp
    private Date joinDate;

    public User(String id, String pwd, String name, String nickname, String email) {
        this.id = id;
        this.pwd = pwd;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
    }
}