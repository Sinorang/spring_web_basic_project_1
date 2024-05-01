package com.elice.boardproject.acc.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @Pattern(regexp = "[a-z0-9]{6,14}$", message = "ID는 6자 이상, 14자 이하의 영어 소문자와 숫자만 입력할 수 있습니다.")
    private String id;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{6,20}$", message = "비밀번호는 6자 이상, 20자 이하의 영어 소문자, 대문자, 특수문자, 숫자를 포함해야 합니다.")
    private String pwd;

    @Pattern(regexp = "^[가-힣]{1,6}$", message = "이름은 6자 이하의 한글만 입력 할 수 있습니다.")
    private String name;

    @Pattern(regexp = "^.{1,10}$", message = "10글자 이하의 모든 문자, 숫자, 특수문자를 사용하세요.")
    private String nickname;

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    public User toEntity() {
        return User.builder()
                .id(id)
                .pwd(pwd)
                .name(name)
                .nickname(nickname)
                .email(email)
                .build();
    }
}
