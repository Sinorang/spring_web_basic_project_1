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
import com.elice.boardproject.security.service.RefreshTokenService;
import com.elice.boardproject.exception.PliException;
import com.elice.boardproject.exception.ErrorCode;

@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public UserController(UserService userService, JwtTokenUtil jwtTokenUtil, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @GetMapping("/")
    public String home() {
        return "acc/index";
    }

    @GetMapping("/acc/index")
    public String indexPage(HttpServletRequest request, Model model) {
        // 회원가입 성공 메시지 확인
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("signupSuccess".equals(cookie.getName()) && "true".equals(cookie.getValue())) {
                    model.addAttribute("signupSuccess", true);
                    break;
                }
            }
        }
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
    public String signUp(@Valid UserDTO userDTO, Errors errors, Model model, HttpServletResponse response) {
        if (errors.hasErrors()) {
            System.out.println(errors.getAllErrors());
            model.addAttribute("userDTO", userDTO);
            return "acc/signup";
        }
        userService.join(userDTO);
        
        // 회원가입 성공 메시지를 쿠키에 저장
        Cookie successCookie = new Cookie("signupSuccess", "true");
        successCookie.setPath("/");
        successCookie.setMaxAge(5); // 5초 후 자동 삭제
        response.addCookie(successCookie);
        
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
        
        try {
            logger.debug("로그인 검증 시작 - ID: {}", loginDTO.getId());
            List<User> loginUser = userService.getLoginUser(loginDTO.getId(), loginDTO.getPwd());
            
            if (loginUser.isEmpty()) {
                logger.warn("로그인 실패 - ID: {}, 비밀번호 불일치 또는 사용자 없음", loginDTO.getId());
                model.addAttribute("loginError", "로그인 정보가 일치하지 않습니다.");
                return "acc/login";
            }
            
            logger.info("로그인 성공 - ID: {}, 사용자명: {}", loginDTO.getId(), loginUser.get(0).getName());
            
            // JWT AccessToken 생성 및 쿠키 설정
            String accessToken = com.elice.boardproject.security.JwtUtil.generateAccessToken(loginDTO.getId());
            logger.debug("AccessToken 생성 완료 - ID: {}", loginDTO.getId());
            
            Cookie jwtCookie = new Cookie("jwt_token", accessToken);
            jwtCookie.setHttpOnly(false); // JavaScript에서 읽을 수 있도록 false로 설정
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(1800); // 30분
            response.addCookie(jwtCookie);
            
            // RefreshToken 생성 및 저장
            refreshTokenService.createRefreshToken(loginDTO.getId(), 7); // 7일
            logger.debug("RefreshToken 생성 완료 - ID: {}", loginDTO.getId());
            
            // RefreshToken 쿠키 설정
            Cookie refreshTokenCookie = new Cookie("refresh_token", refreshTokenService.findByUserId(loginDTO.getId()).get().getToken());
            refreshTokenCookie.setHttpOnly(false); // JavaScript에서 읽을 수 있도록 false로 설정
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(604800); // 7일
            response.addCookie(refreshTokenCookie);
            
            logger.info("JWT 쿠키 설정 완료 - ID: {}", loginDTO.getId());
            return "redirect:/acc/index";
            
        } catch (PliException e) {
            logger.warn("로그인 실패 - ID: {}, 오류 코드: {}", loginDTO.getId(), e.getErrorCode());
            String errorMessage;
            switch (e.getErrorCode()) {
                case ACCOUNT_SUSPENDED:
                    errorMessage = "정지된 계정입니다. 관리자에게 문의하세요.";
                    break;
                case ACCOUNT_WITHDRAWN:
                    errorMessage = "탈퇴된 계정입니다.";
                    break;
                case ACCOUNT_INACTIVE:
                    errorMessage = "비활성화된 계정입니다.";
                    break;
                case USER_NOT_FOUND:
                case INVALID_CREDENTIALS:
                default:
                    errorMessage = "로그인 정보가 일치하지 않습니다.";
                    break;
            }
            model.addAttribute("loginError", errorMessage);
            return "acc/login";
        } catch (Exception e) {
            logger.warn("로그인 실패 - ID: {}, 오류: {}", loginDTO.getId(), e.getMessage());
            model.addAttribute("loginError", "로그인 정보가 일치하지 않습니다.");
            return "acc/login";
        }
    }

    @GetMapping("/acc/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        logger.info("로그아웃 요청");
        
        // 현재 사용자 ID 추출하여 RefreshToken 삭제
        String userId = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (com.elice.boardproject.security.JwtUtil.validateToken(token)) {
                        userId = com.elice.boardproject.security.JwtUtil.getUsernameFromToken(token);
                    }
                    break;
                }
            }
        }
        
        // RefreshToken 삭제
        if (userId != null) {
            refreshTokenService.deleteByUserId(userId);
            logger.info("RefreshToken 삭제 완료 - 사용자: {}", userId);
        }
        
        // JWT 쿠키 삭제
        Cookie jwtCookie = new Cookie("jwt_token", null);
        jwtCookie.setHttpOnly(false);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // 쿠키 즉시 삭제
        response.addCookie(jwtCookie);
        
        // RefreshToken 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(false);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 쿠키 즉시 삭제
        response.addCookie(refreshTokenCookie);
        
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
        userService.updateUserProfile(userId, userProfileUpdateDTO);
        return new ProfileUpdateResponse(true, "프로필이 성공적으로 수정되었습니다.");
    }

    @PostMapping("/api/profile/change-password")
    @ResponseBody
    public PasswordChangeResponse changePassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        userService.changePassword(userId, passwordChangeDTO);
        return new PasswordChangeResponse(true, "비밀번호가 성공적으로 변경되었습니다.");
    }

    @GetMapping("/acc/login-required")
    public String loginRequired() {
        return "acc/login-required";
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