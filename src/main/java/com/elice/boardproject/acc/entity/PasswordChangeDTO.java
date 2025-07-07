package com.elice.boardproject.acc.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDTO {
    
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;
    
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{6,20}$",
        message = "비밀번호는 6자 이상, 20자 이하의 영어 소문자, 대문자, 특수문자, 숫자를 포함해야 합니다."
    )
    private String newPassword;
    
    @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
    private String confirmPassword;
    
    // 비밀번호 일치 확인 메서드
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
    
    @Override
    public String toString() {
        return "PasswordChangeDTO{" +
                "currentPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                '}';
    }
} 