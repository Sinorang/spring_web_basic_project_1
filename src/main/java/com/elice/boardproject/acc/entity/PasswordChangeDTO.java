package com.elice.boardproject.acc.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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
    
    // 기본 생성자
    public PasswordChangeDTO() {}
    
    // 생성자
    public PasswordChangeDTO(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
    
    // Getter와 Setter
    public String getCurrentPassword() {
        return currentPassword;
    }
    
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
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