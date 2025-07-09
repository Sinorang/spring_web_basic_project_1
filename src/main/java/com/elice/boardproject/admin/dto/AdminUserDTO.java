package com.elice.boardproject.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminUserDTO {
    private Long idx;
    private String id;
    private String name;
    private String nickname;
    private String email;
    private LocalDateTime joinDate;
    private String oauthProvider;
    private String oauthId;
    private String oauthEmail;
    private boolean isAdmin;
    private String adminRoleName;
    private LocalDateTime adminGrantedAt;
    private String adminGrantedBy;
} 