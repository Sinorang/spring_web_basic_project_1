package com.elice.boardproject.acc.repository;

import com.elice.boardproject.acc.entity.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Logger logger = LoggerFactory.getLogger(UserRepository.class);
    
    List<User> findAll();
    // 아이디로 사용자 조회 (JWT 인증용)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    User findById(@Param("id") String id);
    
    // OAuth 관련 메서드들
    @Query("SELECT u FROM User u WHERE u.oauthProvider = :provider AND u.oauthId = :oauthId")
    User findByOauthProviderAndOauthId(@Param("provider") String provider, @Param("oauthId") String oauthId);
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    User findByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.nickname = :nickname")
    User findByNickname(@Param("nickname") String nickname);
}
