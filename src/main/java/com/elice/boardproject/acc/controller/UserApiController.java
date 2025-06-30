package com.elice.boardproject.acc.controller;

import com.elice.boardproject.JwtUtil;
import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.entity.UserDetailsImpl;
import com.elice.boardproject.acc.service.UserDetailsServiceImpl;
import com.elice.boardproject.acc.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/acc")
public class UserApiController {
    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;

    public UserApiController(UserService userService, UserDetailsServiceImpl userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    // JWT 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        // 1. 사용자 인증
        List<User> loginUser = userService.getLoginUser(userDTO.getId(), userDTO.getPwd());
        if (loginUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 일치하지 않습니다.");
        }
        // 2. JWT 토큰 생성
        UserDetailsImpl userDetails = new UserDetailsImpl(loginUser.get(0));
        String token = JwtUtil.generateToken(userDetails.getUsername());
        return ResponseEntity.ok().body("Bearer " + token);
    }

    // JWT 로그아웃 (실제 서버 처리 없음, 프론트에서 토큰 삭제)
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok().body("클라이언트에서 토큰을 삭제하세요.");
    }
} 