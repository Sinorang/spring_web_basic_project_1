package com.elice.boardproject.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminRoleDTO {
    private Long id;
    private String roleName;
    private String description;
    private Set<String> permissionNames;
    private LocalDateTime createdAt;
} 