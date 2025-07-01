package com.elice.boardproject.acc.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.repository.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void join(UserDTO userDTO) {
        String hashedPwd = passwordEncoder.encode(userDTO.getPwd());
        User user = new User(userDTO.getId(), hashedPwd, userDTO.getName(), userDTO.getNickname(), userDTO.getEmail());
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getLoginUser(String id, String rawPwd) {
        logger.debug("로그인 검증 시작 - ID: {}", id);
        
        User user = userRepository.findById(id);
        if (user == null) {
            logger.warn("사용자를 찾을 수 없음 - ID: {}", id);
            return List.of();
        }
        
        logger.debug("사용자 발견 - ID: {}, 저장된 해시: {}", id, user.getPwd());
        
        boolean passwordMatches = passwordEncoder.matches(rawPwd, user.getPwd());
        logger.debug("비밀번호 검증 결과 - ID: {}, 일치: {}", id, passwordMatches);
        
        if (passwordMatches) {
            logger.info("로그인 검증 성공 - ID: {}, 사용자명: {}", id, user.getName());
            return List.of(user);
        } else {
            logger.warn("비밀번호 불일치 - ID: {}", id);
            return List.of();
        }
    }

    public User getUserById(String id) {
        return userRepository.findById(id);
    }
}
