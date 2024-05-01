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
    // user
    @Query("SELECT u FROM User u WHERE u.isDel = 0 AND u.isAdmin = 0 AND u.id = :id AND u.pwd = :pwd")
    List<User> findDelByIdAndPwd(@Param("id") String id, @Param("pwd") String pwd);

    // admin
    @Query("SELECT u FROM User u WHERE u.isDel = 0 AND u.isAdmin = 1 AND u.id = :id AND u.pwd = :pwd")
    List<User> findAdminByIdAndPwd(@Param("id") String id, @Param("pwd") String pwd);
}
