package com.elice.boardproject.acc.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.entity.UserProfileUpdateDTO;
import com.elice.boardproject.acc.entity.PasswordChangeDTO;
import com.elice.boardproject.acc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UserProfileUpdateDTO testProfileUpdateDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .idx(1L)
                .id("testuser")
                .pwd("hashedPassword")
                .name("홍길동")
                .nickname("테스트닉네임")
                .email("test@example.com")
                .joinDate(LocalDateTime.now())
                .build();

        testUserDTO = new UserDTO();
        testUserDTO.setId("testuser");
        testUserDTO.setPwd("password123!");
        testUserDTO.setName("홍길동");
        testUserDTO.setNickname("테스트닉네임");
        testUserDTO.setEmail("test@example.com");
        
        testProfileUpdateDTO = new UserProfileUpdateDTO();
        testProfileUpdateDTO.setNickname("새닉네임");
        testProfileUpdateDTO.setEmail("new@example.com");
    }

    @Test
    void 사용자_프로필_조회_성공() {
        // given
        String userId = "testuser";
        when(userRepository.findById(userId)).thenReturn(testUser);

        // when
        User result = userService.getUserById(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getNickname()).isEqualTo("테스트닉네임");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void 프로필_수정_성공() {
        // given
        when(userRepository.findById("testuser")).thenReturn(testUser);
        when(userRepository.findByEmail("new@example.com")).thenReturn(null);
        when(userRepository.findByNickname("새닉네임")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        userService.updateUserProfile("testuser", testProfileUpdateDTO);

        // then
        verify(userRepository).findById("testuser");
        verify(userRepository).findByEmail("new@example.com");
        verify(userRepository).findByNickname("새닉네임");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 존재하지_않는_사용자_프로필_수정_실패() {
        // given
        when(userRepository.findById("nonexistent")).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> userService.updateUserProfile("nonexistent", testProfileUpdateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    void 이메일_중복_검증_실패() {
        // given
        User existingUser = User.builder().id("otheruser").email("new@example.com").build();
        when(userRepository.findById("testuser")).thenReturn(testUser);
        when(userRepository.findByEmail("new@example.com")).thenReturn(existingUser);

        // when & then
        assertThatThrownBy(() -> userService.updateUserProfile("testuser", testProfileUpdateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    void 닉네임_중복_검증_실패() {
        // given
        User existingUser = User.builder().id("otheruser").nickname("새닉네임").build();
        when(userRepository.findById("testuser")).thenReturn(testUser);
        when(userRepository.findByEmail("new@example.com")).thenReturn(null);
        when(userRepository.findByNickname("새닉네임")).thenReturn(existingUser);

        // when & then
        assertThatThrownBy(() -> userService.updateUserProfile("testuser", testProfileUpdateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");
    }

    @Test
    void 같은_이메일로_프로필_수정_성공() {
        // given
        testProfileUpdateDTO.setEmail("test@example.com"); // 기존 이메일과 동일
        when(userRepository.findById("testuser")).thenReturn(testUser);
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser); // 자신의 이메일
        when(userRepository.findByNickname("새닉네임")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        userService.updateUserProfile("testuser", testProfileUpdateDTO);

        // then
        verify(userRepository).findById("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).findByNickname("새닉네임");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 같은_닉네임으로_프로필_수정_성공() {
        // given
        testProfileUpdateDTO.setNickname("테스트닉네임"); // 기존 닉네임과 동일
        when(userRepository.findById("testuser")).thenReturn(testUser);
        when(userRepository.findByEmail("new@example.com")).thenReturn(null);
        when(userRepository.findByNickname("테스트닉네임")).thenReturn(testUser); // 자신의 닉네임
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        userService.updateUserProfile("testuser", testProfileUpdateDTO);

        // then
        verify(userRepository).findById("testuser");
        verify(userRepository).findByEmail("new@example.com");
        verify(userRepository).findByNickname("테스트닉네임");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 비밀번호_변경_성공() {
        // given
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setCurrentPassword("oldPassword123!");
        passwordChangeDTO.setNewPassword("newPassword123!");
        passwordChangeDTO.setConfirmPassword("newPassword123!");

        when(userRepository.findById("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("oldPassword123!", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPassword123!", "hashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123!")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        userService.changePassword("testuser", passwordChangeDTO);

        // then
        verify(userRepository).findById("testuser");
        verify(passwordEncoder).matches("oldPassword123!", "hashedPassword");
        verify(passwordEncoder).matches("newPassword123!", "hashedPassword");
        verify(passwordEncoder).encode("newPassword123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 존재하지_않는_사용자_비밀번호_변경_실패() {
        // given
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setCurrentPassword("oldPassword123!");
        passwordChangeDTO.setNewPassword("newPassword123!");
        passwordChangeDTO.setConfirmPassword("newPassword123!");

        when(userRepository.findById("nonexistent")).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> userService.changePassword("nonexistent", passwordChangeDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    void 현재_비밀번호_불일치_비밀번호_변경_실패() {
        // given
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setCurrentPassword("wrongPassword123!");
        passwordChangeDTO.setNewPassword("newPassword123!");
        passwordChangeDTO.setConfirmPassword("newPassword123!");

        when(userRepository.findById("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongPassword123!", "hashedPassword")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.changePassword("testuser", passwordChangeDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 새_비밀번호_불일치_비밀번호_변경_실패() {
        // given
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setCurrentPassword("oldPassword123!");
        passwordChangeDTO.setNewPassword("newPassword123!");
        passwordChangeDTO.setConfirmPassword("differentPassword123!");

        when(userRepository.findById("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("oldPassword123!", "hashedPassword")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.changePassword("testuser", passwordChangeDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 같은_비밀번호로_변경_시도_실패() {
        // given
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setCurrentPassword("oldPassword123!");
        passwordChangeDTO.setNewPassword("oldPassword123!");
        passwordChangeDTO.setConfirmPassword("oldPassword123!");

        when(userRepository.findById("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("oldPassword123!", "hashedPassword")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.changePassword("testuser", passwordChangeDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
    }

    @Test
    void 회원가입_성공() {
        // given
        when(passwordEncoder.encode("password123!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        userService.join(testUserDTO);

        // then
        verify(passwordEncoder).encode("password123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 로그인_성공() {
        // given
        when(userRepository.findById("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password123!", "hashedPassword")).thenReturn(true);

        // when
        List<User> result = userService.getLoginUser("testuser", "password123!");

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).isEqualTo(testUser);
        verify(userRepository).findById("testuser");
        verify(passwordEncoder).matches("password123!", "hashedPassword");
    }

    @Test
    void 존재하지_않는_사용자_로그인_실패() {
        // given
        when(userRepository.findById("nonexistent")).thenReturn(null);

        // when
        List<User> result = userService.getLoginUser("nonexistent", "password123!");

        // then
        assertThat(result).isEmpty();
        verify(userRepository).findById("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void 비밀번호_불일치_로그인_실패() {
        // given
        when(userRepository.findById("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        // when
        List<User> result = userService.getLoginUser("testuser", "wrongPassword");

        // then
        assertThat(result).isEmpty();
        verify(userRepository).findById("testuser");
        verify(passwordEncoder).matches("wrongPassword", "hashedPassword");
    }

    @Test
    void 모든_사용자_조회_성공() {
        // given
        List<User> users = List.of(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // when
        List<User> result = userService.getAllUsers();

        // then
        assertThat(result).isEqualTo(users);
        verify(userRepository).findAll();
    }

    @Test
    void 아이디_수정_시도_실패() {
        // given
        UserDTO updateDTO = new UserDTO();
        updateDTO.setId("newid");

        when(userRepository.findById("testuser")).thenReturn(testUser);

        // when & then
        assertThatThrownBy(() -> userService.updateUserProfile("testuser", updateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디는 수정할 수 없습니다.");
    }

    @Test
    void 이름_수정_시도_실패() {
        // given
        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("새이름");

        when(userRepository.findById("testuser")).thenReturn(testUser);

        // when & then
        assertThatThrownBy(() -> userService.updateUserProfile("testuser", updateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 수정할 수 없습니다.");
    }

    @Test
    void 가입일_형식_테스트() {
        // given
        LocalDateTime joinDate = LocalDateTime.of(2025, 7, 4, 10, 30, 0);
        User userWithJoinDate = User.builder()
                .idx(1L)
                .id("testuser")
                .pwd("hashedPassword")
                .name("홍길동")
                .nickname("테스트닉네임")
                .email("test@example.com")
                .joinDate(joinDate)
                .build();

        // when
        LocalDateTime result = userWithJoinDate.getJoinDate();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(2025);
        assertThat(result.getMonthValue()).isEqualTo(7);
        assertThat(result.getDayOfMonth()).isEqualTo(4);
    }
} 