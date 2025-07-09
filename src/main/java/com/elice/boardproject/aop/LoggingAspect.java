package com.elice.boardproject.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // 서비스 계층(@Service)만 AOP 적용
    @Around("execution(* com.elice.boardproject..service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long end = System.currentTimeMillis();
            logger.info("[AOP-LOG] Method: {}, Params: {}, Result: {}, Time: {}ms",
                    methodName, Arrays.toString(args), result, (end - start));
            return result;
        } catch (Throwable ex) {
            long end = System.currentTimeMillis();
            logger.error("[AOP-LOG] Method: {}, Params: {}, Exception: {}, Time: {}ms",
                    methodName, Arrays.toString(args), ex.toString(), (end - start));
            throw ex;
        }
    }
} 