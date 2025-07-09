package com.elice.boardproject.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminUserStatisticsDTO {
    private long totalUsers;
    private long adminUsers;
    private long oauthUsers;
    private long regularUsers;
} 