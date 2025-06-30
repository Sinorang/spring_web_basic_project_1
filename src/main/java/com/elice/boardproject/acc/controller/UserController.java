package com.elice.boardproject.acc.controller;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.service.UserService;
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
            System.out.println(errors.getAllErrors());
            model.addAttribute("userDTO", userDTO);
            return "acc/signup";
        }
        userService.join(userDTO);
        return "redirect:/acc/index";
    }
}