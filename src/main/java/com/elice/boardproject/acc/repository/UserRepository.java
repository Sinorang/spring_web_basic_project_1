package com.elice.boardproject.acc.repository;

import com.elice.boardproject.acc.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll();

//    User findUserById(String id);

    // 회원가입
    User save(User user);

    // 로그인
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.pwd = :pwd")
    List<User> findUserByIdAndPwd(@Param("id") String id, @Param("pwd") String pwd);

    // 아이디로 사용자 조회 (JWT 인증용)
    User findById(String id);
}
