package com.elice.boardproject.acc.repository;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserStatus;
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
    
    // 아이디로 사용자 조회 (JWT 인증용) - 활성 사용자만
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true")
    User findById(@Param("id") String id);
    
    // JWT 인증 전용 - AdminRole과 Permission을 함께 조회
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.adminRole ar LEFT JOIN FETCH ar.permissions WHERE u.id = :id AND u.isActive = true")
    User findByIdWithAdminRole(@Param("id") String id);
    
    // 사용자 조회 (활성/비활성 구분 없음)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    User findByIdForAdmin(@Param("id") String id);
    
    // OAuth 관련 메서드들 - 활성 사용자만
    @Query("SELECT u FROM User u WHERE u.oauthProvider = :provider AND u.oauthId = :oauthId AND u.isActive = true")
    User findByOauthProviderAndOauthId(@Param("provider") String provider, @Param("oauthId") String oauthId);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    User findByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.nickname = :nickname AND u.isActive = true")
    User findByNickname(@Param("nickname") String nickname);
    
    // 상태별 사용자 조회 (관리자용)
    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findByStatus(@Param("status") UserStatus status);
    
    // 관리자 통계용 메서드들
    long countByIsAdminTrue();
    
    long countByOauthProviderIsNotNull();
    
    // 상태별 사용자 수 집계
    long countByStatus(UserStatus status);
    
    // 활성 사용자 수 집계
    long countByIsActiveTrue();
}
