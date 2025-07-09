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
    List<User> findAllByIsActiveTrue();
    // 아이디로 사용자 조회 (JWT 인증용)
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true")
    User findById(@Param("id") String id);
    
    // OAuth 관련 메서드들
    @Query("SELECT u FROM User u WHERE u.oauthProvider = :provider AND u.oauthId = :oauthId AND u.isActive = true")
    User findByOauthProviderAndOauthId(@Param("provider") String provider, @Param("oauthId") String oauthId);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    User findByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.nickname = :nickname AND u.isActive = true")
    User findByNickname(@Param("nickname") String nickname);
    
    // 관리자 통계용 메서드들
    long countByIsAdminTrue();
    
    long countByOauthProviderIsNotNull();
}
