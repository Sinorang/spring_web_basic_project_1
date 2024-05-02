package com.elice.boardproject.acc.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User join(UserDTO userDTO) {
        return userRepository.save(userDTO.toEntity());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getLoginUser(String id, String pwd) {
        return userRepository.findUserByIdAndPwd(id, pwd);
    }

//    public User getUserById(String id) {
//        return userRepository.findUserById(id);
//    }
}
