package com.elice.boardproject.security;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import java.util.List;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private BoardService boardService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            User loginUser = jwtTokenUtil.getCurrentUser(request);
            if (loginUser != null) {
                modelAndView.addObject("loginId", loginUser.getId());
                modelAndView.addObject("loginNickname", loginUser.getNickname());
            }
            
            // 모든 페이지에서 게시판 목록을 모델에 추가
            List<Board> allBoards = boardService.getAllBoards();
            modelAndView.addObject("allBoards", allBoards);
        }
    }
} 