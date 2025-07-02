package com.elice.boardproject.security;

import com.elice.boardproject.acc.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            User loginUser = jwtTokenUtil.getCurrentUser(request);
            if (loginUser != null) {
                modelAndView.addObject("loginId", loginUser.getId());
                modelAndView.addObject("loginNickname", loginUser.getNickname());
            }
        }
    }
} 