package com.elice.boardproject.aop;

import com.elice.boardproject.aop.annotation.RequirePermission;
import com.elice.boardproject.admin.service.AdminUserService;
import com.elice.boardproject.exception.ErrorCode;
import com.elice.boardproject.exception.PliException;
import com.elice.boardproject.security.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionAspect {

    private final AdminUserService adminUserService;
    private final JwtTokenUtil jwtTokenUtil;

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        String requiredPermission = requirePermission.value();
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new PliException(ErrorCode.UNAUTHORIZED, "요청 정보를 찾을 수 없습니다.");
        }
        
        HttpServletRequest request = attributes.getRequest();
        var currentUser = jwtTokenUtil.getCurrentUser(request);
        
        if (currentUser == null) {
            throw new PliException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        
        boolean hasPermission = adminUserService.hasPermission(currentUser.getIdx(), requiredPermission);
        if (!hasPermission) {
            log.warn("사용자 {}가 권한 {} 없이 접근 시도", currentUser.getId(), requiredPermission);
            throw new PliException(ErrorCode.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다.");
        }
        
        log.debug("사용자 {}가 권한 {}로 접근", currentUser.getId(), requiredPermission);
    }
} 