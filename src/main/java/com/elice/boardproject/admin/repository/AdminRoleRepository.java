package com.elice.boardproject.admin.repository;

import com.elice.boardproject.admin.entity.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {
    Optional<AdminRole> findByRoleName(String roleName);
    boolean existsByRoleName(String roleName);
} 