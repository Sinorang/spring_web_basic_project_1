package com.elice.boardproject.acc.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.entity.UserProfileUpdateDTO;
import com.elice.boardproject.acc.entity.PasswordChangeDTO;
import com.elice.boardproject.acc.entity.UserStatus;
import com.elice.boardproject.acc.dto.UserWithdrawRequestDTO;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.exception.ErrorCode;
import com.elice.boardproject.exception.ExceptionUtils;
import com.elice.boardproject.exception.PliException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        
        User user = ExceptionUtils.requireNonNull(userRepository.findByIdForAdmin(id), ErrorCode.USER_NOT_FOUND, id);
        
        logger.debug("사용자 발견 - ID: {}, 저장된 해시: {}", id, user.getPwd());
        
        // 사용자 상태별 처리 (isActive 체크보다 먼저)
        switch (user.getStatus()) {
            case SUSPENDED:
                logger.warn("정지된 계정 로그인 시도 - ID: {}", id);
                ExceptionUtils.throwException(ErrorCode.ACCOUNT_SUSPENDED);
                return List.of(); // 이 라인은 실행되지 않음
            case WITHDRAWN:
                logger.warn("탈퇴된 계정 로그인 시도 - ID: {}", id);
                ExceptionUtils.throwException(ErrorCode.ACCOUNT_WITHDRAWN);
                return List.of(); // 이 라인은 실행되지 않음
            case ACTIVE:
                // 정상 상태이므로 계속 진행
                break;
            default:
                logger.warn("알 수 없는 계정 상태 - ID: {}, 상태: {}", id, user.getStatus());
                ExceptionUtils.throwException(ErrorCode.ACCOUNT_INACTIVE);
                return List.of(); // 이 라인은 실행되지 않음
        }
        
        // isActive 체크는 마지막에
        if (!user.isActive()) {
            logger.warn("비활성화된 계정 로그인 시도 - ID: {}", id);
            ExceptionUtils.throwException(ErrorCode.ACCOUNT_INACTIVE);
            return List.of(); // 이 라인은 실행되지 않음
        }
        
        boolean passwordMatches = passwordEncoder.matches(rawPwd, user.getPwd());
        logger.debug("비밀번호 검증 결과 - ID: {}, 일치: {}", id, passwordMatches);
        
        if (passwordMatches) {
            logger.info("로그인 검증 성공 - ID: {}, 사용자명: {}", id, user.getName());
            return List.of(user);
        } else {
            logger.warn("비밀번호 불일치 - ID: {}", id);
            ExceptionUtils.throwException(ErrorCode.INVALID_CREDENTIALS);
            return List.of(); // 이 라인은 실행되지 않음
        }
    }

    public User getUserById(String id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void updateUserProfile(String userId, UserDTO updateUserDTO) {
        User user = ExceptionUtils.requireNonNull(userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);

        // 수정 불가 필드 검증
        if (updateUserDTO.getId() != null && !updateUserDTO.getId().trim().isEmpty()) {
            ExceptionUtils.throwException(ErrorCode.INVALID_INPUT_VALUE, "아이디는 수정할 수 없습니다.");
        }
        
        if (updateUserDTO.getName() != null && !updateUserDTO.getName().trim().isEmpty()) {
            ExceptionUtils.throwException(ErrorCode.INVALID_INPUT_VALUE, "이름은 수정할 수 없습니다.");
        }

        // 이메일 중복 검증 (자신의 이메일이 아닌 경우)
        if (updateUserDTO.getEmail() != null && !updateUserDTO.getEmail().trim().isEmpty()) {
            User existingUserByEmail = userRepository.findByEmail(updateUserDTO.getEmail());
            if (existingUserByEmail != null && !existingUserByEmail.getId().equals(userId)) {
                ExceptionUtils.throwException(ErrorCode.EMAIL_ALREADY_EXISTS, updateUserDTO.getEmail());
            }
            user.setEmail(updateUserDTO.getEmail());
        }

        // 닉네임 중복 검증 (자신의 닉네임이 아닌 경우)
        if (updateUserDTO.getNickname() != null && !updateUserDTO.getNickname().trim().isEmpty()) {
            User existingUserByNickname = userRepository.findByNickname(updateUserDTO.getNickname());
            if (existingUserByNickname != null && !existingUserByNickname.getId().equals(userId)) {
                ExceptionUtils.throwException(ErrorCode.NICKNAME_ALREADY_EXISTS, updateUserDTO.getNickname());
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

    @Transactional
    public void updateUserProfile(String userId, UserProfileUpdateDTO updateDTO) {
        User user = ExceptionUtils.requireNonNull(userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);

        // 이메일 중복 검증 (자신의 이메일이 아닌 경우)
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().trim().isEmpty()) {
            User existingUserByEmail = userRepository.findByEmail(updateDTO.getEmail());
            if (existingUserByEmail != null && !existingUserByEmail.getId().equals(userId)) {
                ExceptionUtils.throwException(ErrorCode.EMAIL_ALREADY_EXISTS, updateDTO.getEmail());
            }
            user.setEmail(updateDTO.getEmail());
        }

        // 닉네임 중복 검증 (자신의 닉네임이 아닌 경우)
        if (updateDTO.getNickname() != null && !updateDTO.getNickname().trim().isEmpty()) {
            User existingUserByNickname = userRepository.findByNickname(updateDTO.getNickname());
            if (existingUserByNickname != null && !existingUserByNickname.getId().equals(userId)) {
                ExceptionUtils.throwException(ErrorCode.NICKNAME_ALREADY_EXISTS, updateDTO.getNickname());
            }
            user.setNickname(updateDTO.getNickname());
        }

        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String userId, PasswordChangeDTO passwordChangeDTO) {
        User user = ExceptionUtils.requireNonNull(userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(passwordChangeDTO.getCurrentPassword(), user.getPwd())) {
            ExceptionUtils.throwException(ErrorCode.INVALID_PASSWORD, "현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호와 확인 비밀번호 일치 확인
        if (!passwordChangeDTO.isPasswordMatch()) {
            ExceptionUtils.throwException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 새 비밀번호가 현재 비밀번호와 다른지 확인
        if (passwordEncoder.matches(passwordChangeDTO.getNewPassword(), user.getPwd())) {
            ExceptionUtils.throwException(ErrorCode.INVALID_PASSWORD, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // 새 비밀번호로 업데이트
        String hashedNewPassword = passwordEncoder.encode(passwordChangeDTO.getNewPassword());
        user.setPwd(hashedNewPassword);
        userRepository.save(user);
        
        logger.info("비밀번호 변경 성공 - 사용자 ID: {}", userId);
    }

    /**
     * 사용자 탈퇴
     */
    @Transactional
    public void withdrawUser(String userId, UserWithdrawRequestDTO withdrawRequest) {
        User user = ExceptionUtils.requireNonNull(userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);
        
        // 탈퇴 처리: status 변경 시 isActive 자동 동기화
        user.setStatus(UserStatus.WITHDRAWN);
        user.setIsActive(false);
        // 탈퇴 사유 저장
        user.setReason(withdrawRequest.getReason());
        
        // 탈퇴 사유 로깅 (개인정보 보호를 위해 DB에는 저장하지 않음)
        if (withdrawRequest.getReason() != null && !withdrawRequest.getReason().trim().isEmpty()) {
            logger.info("사용자 탈퇴 - ID: {}, 사유: {}", userId, withdrawRequest.getReason());
        } else {
            logger.info("사용자 탈퇴 - ID: {}, 사유: 미입력", userId);
        }
        
        userRepository.save(user);
    }
}
