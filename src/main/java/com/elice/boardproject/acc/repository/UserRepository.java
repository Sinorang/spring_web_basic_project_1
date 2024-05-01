package com.elice.boardproject.acc.repository;

import com.elice.boardproject.acc.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 관리자. 회원 목록 조회
    List<User> findAll();

    // 회원가입
    User save(User user);

    // 로그인
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.pwd = :pwd")
    List<User> findUserByIdAndPwd(@Param("id") String id, @Param("pwd") String pwd);
}
