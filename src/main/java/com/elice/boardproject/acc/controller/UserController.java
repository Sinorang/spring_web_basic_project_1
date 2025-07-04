package com.elice.boardproject.acc.controller;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.entity.LoginDTO;
import com.elice.boardproject.acc.entity.UserProfileUpdateDTO;
import com.elice.boardproject.acc.entity.PasswordChangeDTO;
import com.elice.boardproject.acc.service.UserService;
import com.elice.boardproject.security.JwtUtil;
import com.elice.boardproject.security.JwtTokenUtil;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserController(UserService userService, JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/")
    public String home() {
        return "acc/index";
    }

    @GetMapping("/acc/index")
    public String indexPage() {
        return "acc/index";
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
        String token = com.elice.boardproject.security.JwtUtil.generateToken(loginDTO.getId());
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

    @GetMapping("/profile")
    public String profilePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        model.addAttribute("loginId", user.getId());
        model.addAttribute("loginNickname", user.getNickname());
        return "acc/profile-view";
    }

    @GetMapping("/profile/edit")
    public String profileEditPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        model.addAttribute("loginId", user.getId());
        model.addAttribute("loginNickname", user.getNickname());
        return "acc/profile-edit";
    }

    @PostMapping("/api/profile/update")
    @ResponseBody
    public ProfileUpdateResponse updateProfile(@Valid @RequestBody UserProfileUpdateDTO userProfileUpdateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        
        try {
            userService.updateUserProfile(userId, userProfileUpdateDTO);
            return new ProfileUpdateResponse(true, "프로필이 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message.contains("아이디") || message.contains("이름")) {
                return new ProfileUpdateResponse(false, message, new String[]{"id", "name", "joinDate"});
            }
            return new ProfileUpdateResponse(false, message);
        }
    }

    @PostMapping("/api/profile/change-password")
    @ResponseBody
    public PasswordChangeResponse changePassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        
        try {
            userService.changePassword(userId, passwordChangeDTO);
            return new PasswordChangeResponse(true, "비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return new PasswordChangeResponse(false, e.getMessage());
        }
    }

    public static class ProfileUpdateResponse {
        private boolean success;
        private String message;
        private String[] readonlyFields;

        public ProfileUpdateResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public ProfileUpdateResponse(boolean success, String message, String[] readonlyFields) {
            this.success = success;
            this.message = message;
            this.readonlyFields = readonlyFields;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String[] getReadonlyFields() {
            return readonlyFields;
        }

        public void setReadonlyFields(String[] readonlyFields) {
            this.readonlyFields = readonlyFields;
        }
    }

    public static class PasswordChangeResponse {
        private boolean success;
        private String message;

        public PasswordChangeResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}