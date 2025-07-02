package com.elice.boardproject.acc.controller;

import com.elice.boardproject.security.JwtUtil;
import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.entity.UserDetailsImpl;
import com.elice.boardproject.acc.service.UserDetailsServiceImpl;
import com.elice.boardproject.acc.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/acc")
public class UserApiController {
    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;

    public UserApiController(UserService userService, UserDetailsServiceImpl userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    // JWT 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO, HttpServletResponse response) {
        // 1. 사용자 인증
        List<User> loginUser = userService.getLoginUser(userDTO.getId(), userDTO.getPwd());
        if (loginUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 일치하지 않습니다.");
        }
        // 2. JWT 토큰 생성
        UserDetailsImpl userDetails = new UserDetailsImpl(loginUser.get(0));
        String token = JwtUtil.generateToken(userDetails.getUsername());
        
        // 3. 쿠키에 JWT 토큰 설정
        Cookie jwtCookie = new Cookie("jwt_token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // HTTPS 환경에서는 true로 설정
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600); // 1시간
        response.addCookie(jwtCookie);
        
        return ResponseEntity.ok().body("Bearer " + token);
    }

    // JWT 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // 쿠키에서 JWT 토큰 삭제
        Cookie jwtCookie = new Cookie("jwt_token", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // 즉시 삭제
        response.addCookie(jwtCookie);
        
        return ResponseEntity.ok().body("로그아웃되었습니다.");
    }
} 