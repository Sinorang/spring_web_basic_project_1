package com.elice.boardproject.acc.controller;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.entity.LoginDTO;
import com.elice.boardproject.acc.service.UserService;
import com.elice.boardproject.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "acc/index";
    }

    @GetMapping("/acc/index")
    public String indexPage() {
        return "acc/index";
    }

    @ModelAttribute
    public void addLoginUserToModel(HttpServletRequest request, Model model) {
        // JWT 토큰에서 사용자 정보 추출
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        
        if (token != null && JwtUtil.validateToken(token)) {
            try {
                String username = JwtUtil.getUsernameFromToken(token);
                User user = userService.getUserById(username);
                if (user != null) {
                    model.addAttribute("loginUser", user);
                }
            } catch (Exception e) {
                logger.debug("JWT 토큰 파싱 실패: {}", e.getMessage());
            }
        }
    }

    @RequestMapping("/acc/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @RequestMapping("/acc/signup")
    public String signUpPage(UserDTO userDTO) {
        return "acc/signup";
    }

    @GetMapping("/acc/login")
    public String loginPage() {
        return "acc/login";
    }

    @PostMapping("/acc/signup")
    public String signUp(@Valid UserDTO userDTO, Errors errors, Model model) {
        if (errors.hasErrors()) {
            System.out.println(errors.getAllErrors());
            model.addAttribute("userDTO", userDTO);
            return "acc/signup";
        }
        userService.join(userDTO);
        return "redirect:/acc/index";
    }

    @PostMapping("/acc/login")
    public String login(LoginDTO loginDTO, Model model, HttpServletResponse response) {
        logger.info("로그인 시도 - ID: {}", loginDTO.getId());
        
        // 간단한 입력값 검증
        if (loginDTO.getId() == null || loginDTO.getId().trim().isEmpty() || 
            loginDTO.getPwd() == null || loginDTO.getPwd().trim().isEmpty()) {
            logger.warn("로그인 입력값 누락 - ID: {}", loginDTO.getId());
            model.addAttribute("loginError", "아이디와 비밀번호를 모두 입력해주세요.");
            return "acc/login";
        }
        
        logger.debug("로그인 검증 시작 - ID: {}", loginDTO.getId());
        List<User> loginUser = userService.getLoginUser(loginDTO.getId(), loginDTO.getPwd());
        
        if (loginUser.isEmpty()) {
            logger.warn("로그인 실패 - ID: {}, 비밀번호 불일치 또는 사용자 없음", loginDTO.getId());
            model.addAttribute("loginError", "로그인 정보가 일치하지 않습니다.");
            return "acc/login";
        }
        
        logger.info("로그인 성공 - ID: {}, 사용자명: {}", loginDTO.getId(), loginUser.get(0).getName());
        
        // JWT 토큰 생성 및 쿠키 설정
        String token = com.elice.boardproject.JwtUtil.generateToken(loginDTO.getId());
        logger.debug("JWT 토큰 생성 완료 - ID: {}", loginDTO.getId());
        
        Cookie jwtCookie = new Cookie("jwt_token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);
        response.addCookie(jwtCookie);
        
        logger.info("JWT 쿠키 설정 완료 - ID: {}", loginDTO.getId());
        return "redirect:/acc/index";
    }

    @GetMapping("/acc/logout")
    public String logout(HttpServletResponse response) {
        logger.info("로그아웃 요청");
        
        // JWT 쿠키 삭제
        Cookie jwtCookie = new Cookie("jwt_token", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // 쿠키 즉시 삭제
        response.addCookie(jwtCookie);
        
        logger.info("JWT 쿠키 삭제 완료");
        return "redirect:/acc/index";
    }
}