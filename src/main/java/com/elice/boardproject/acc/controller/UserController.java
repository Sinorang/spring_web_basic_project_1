package com.elice.boardproject.acc.controller;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {
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

    @RequestMapping("/acc/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @RequestMapping("/acc/signup")
    public String signUpPage(UserDTO userDTO) {
        return "acc/signup";
    }

    @PostMapping("/acc/signup")
    public String signUp(@Valid UserDTO userDTO, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("userDTO", userDTO);
            return "acc/signup";
        }
        userService.join(userDTO);
        return "redirect:/acc/index";
    }

    @RequestMapping("/acc/login")
    public String loginPage(UserDTO userDTO) {
        return "acc/login";
    }

    @PostMapping("/acc/login")
    public String login(UserDTO userDTO, Model model, HttpSession session) {
        List<User> loginUser = userService.getLoginUser(userDTO.getId(), userDTO.getPwd());
        // 로그인 성공 여부를 체크하고 적절한 처리를 수행
        if (!loginUser.isEmpty()) {
            session.setAttribute("loginUser", loginUser.get(0));
            return "redirect:/acc/index"; // 로그인 성공 시 홈 페이지로 리다이렉트
        } else {
            model.addAttribute("error", "로그인 정보가 일치하지 않습니다."); // 에러 메시지 전달
            return "acc/login"; //로그인 폼 페이지로 다시 이동
        }
    }


    @RequestMapping("/acc/logout")
    public String logout(HttpSession session) {
        // 세션에서 사용자 정보 삭제
        if(session.getAttribute("loginUser") != null) {
            session.invalidate();
        } else if (session.getAttribute("adminLogin") != null) {
            session.invalidate();
        } else {
            // do nothing
        }

        return "redirect:/acc/index"; // 로그아웃 후 홈 페이지로 리다이렉트
    }


    @RequestMapping("/acc/adminLogin")
    public String adminLoginPage() {
        return "acc/admin/login";
    }

    @PostMapping("/acc/adminLogin")
    public String adminLogin(UserDTO userDTO, Model model, HttpSession session) {
        List<User> loginAdmin = userService.getLoginAdmin(userDTO.getId(), userDTO.getPwd());

        if (!loginAdmin.isEmpty()) {
            session.setAttribute("loginAdmin", loginAdmin.get(0));
            return "redirect:/acc/admin/index";
        } else {
            model.addAttribute("error", "관리자 정보를 확인해주세요.");
            return "acc/admin/login";
        }
    }
}