package com.elice.boardproject.acc.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDetailsImpl;
import com.elice.boardproject.acc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // JWT 인증 시에만 AdminRole과 Permission을 함께 조회
        User user = userRepository.findByIdWithAdminRole(username);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
        return new UserDetailsImpl(user);
    }
} 