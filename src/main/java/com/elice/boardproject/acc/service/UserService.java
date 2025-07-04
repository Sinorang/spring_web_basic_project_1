package com.elice.boardproject.acc.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.entity.UserProfileUpdateDTO;
import com.elice.boardproject.acc.entity.PasswordChangeDTO;
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

    public void updateUserProfile(String userId, UserDTO updateUserDTO) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 수정 불가 필드 검증
        if (updateUserDTO.getId() != null && !updateUserDTO.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("아이디는 수정할 수 없습니다.");
        }
        
        if (updateUserDTO.getName() != null && !updateUserDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 수정할 수 없습니다.");
        }

        // 이메일 중복 검증 (자신의 이메일이 아닌 경우)
        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().trim().isEmpty()) {
            User existingUserByEmail = userRepository.findByEmail(updateUserDTO.getEmail());
            if (existingUserByEmail != null && !existingUserByEmail.getId().equals(userId)) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            user.setEmail(updateUserDTO.getEmail());
        }

        // 닉네임 중복 검증 (자신의 닉네임이 아닌 경우)
        if (updateUserDTO.getNickname() != null && !updateUserDTO.getNickname().trim().isEmpty()) {
            User existingUserByNickname = userRepository.findByNickname(updateUserDTO.getNickname());
            if (existingUserByNickname != null && !existingUserByNickname.getId().equals(userId)) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(updateUserDTO.getNickname());
        }

        // 비밀번호 변경 (제공된 경우에만)
        if (updateUserDTO.getPwd() != null && !updateUserDTO.getPwd().trim().isEmpty()) {
            String hashedPwd = passwordEncoder.encode(updateUserDTO.getPwd());
            user.setPwd(hashedPwd);
        }

        userRepository.save(user);
    }

    public void updateUserProfile(String userId, UserProfileUpdateDTO updateDTO) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 이메일 중복 검증 (자신의 이메일이 아닌 경우)
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().trim().isEmpty()) {
            User existingUserByEmail = userRepository.findByEmail(updateDTO.getEmail());
            if (existingUserByEmail != null && !existingUserByEmail.getId().equals(userId)) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            user.setEmail(updateDTO.getEmail());
        }

        // 닉네임 중복 검증 (자신의 닉네임이 아닌 경우)
        if (updateDTO.getNickname() != null && !updateDTO.getNickname().trim().isEmpty()) {
            User existingUserByNickname = userRepository.findByNickname(updateDTO.getNickname());
            if (existingUserByNickname != null && !existingUserByNickname.getId().equals(userId)) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(updateDTO.getNickname());
        }

        userRepository.save(user);
    }

    public void changePassword(String userId, PasswordChangeDTO passwordChangeDTO) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(passwordChangeDTO.getCurrentPassword(), user.getPwd())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호와 확인 비밀번호 일치 확인
        if (!passwordChangeDTO.isPasswordMatch()) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호가 현재 비밀번호와 다른지 확인
        if (passwordEncoder.matches(passwordChangeDTO.getNewPassword(), user.getPwd())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // 새 비밀번호로 업데이트
        String hashedNewPassword = passwordEncoder.encode(passwordChangeDTO.getNewPassword());
        user.setPwd(hashedNewPassword);
        userRepository.save(user);
        
        logger.info("비밀번호 변경 성공 - 사용자 ID: {}", userId);
    }
}
