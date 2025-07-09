package com.elice.boardproject.acc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 탈퇴 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserWithdrawRequestDTO {
    private String reason;  // 탈퇴 사유 (선택사항)
} 