package com.elice.boardproject.acc.repository;

import com.elice.boardproject.acc.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll();
    // 아이디로 사용자 조회 (JWT 인증용)
    User findById(String id);
}
