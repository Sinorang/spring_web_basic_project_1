package com.elice.boardproject.acc.entity;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

public class UserDetailsImpl implements UserDetails {
    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // 관리자인 경우 ADMIN 역할 추가
        if (user.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        
        // AdminRole의 권한들 추가
        if (user.getAdminRole() != null && user.getAdminRole().getPermissions() != null) {
            authorities.addAll(user.getAdminRole().getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p.getPermissionName()))
                .collect(Collectors.toList()));
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPwd();
    }

    @Override
    public String getUsername() {
        return user.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 활성 사용자만 로그인 가능 (isActive=true)
        return user.isActive();
    }
} 