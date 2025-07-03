package com.elice.boardproject.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/oauth")
public class OAuthController {

    @GetMapping("/error")
    public String oauthError() {
        return "oauth/error";
    }
} 