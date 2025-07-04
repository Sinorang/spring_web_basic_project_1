package com.elice.boardproject.acc.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserProfileUpdateDTO {
    
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Pattern(regexp = "^.{1,10}$", message = "닉네임은 10글자 이하로 입력해주세요.")
    private String nickname;
    
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    private String email;
    
    // 기본 생성자
    public UserProfileUpdateDTO() {}
    
    // 생성자
    public UserProfileUpdateDTO(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }
    
    // Getter와 Setter
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public String toString() {
        return "UserProfileUpdateDTO{" +
                "nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
} 