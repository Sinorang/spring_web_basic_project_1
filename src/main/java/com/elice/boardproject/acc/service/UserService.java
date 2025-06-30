package com.elice.boardproject.acc.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

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
        User user = userRepository.findById(id);
        if (user != null && passwordEncoder.matches(rawPwd, user.getPwd())) {
            return List.of(user);
        }
        return List.of();
    }

//    public User getUserById(String id) {
//        return userRepository.findUserById(id);
//    }
}
