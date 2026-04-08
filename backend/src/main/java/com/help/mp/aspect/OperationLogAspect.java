package com.help.mp.aspect;

import com.help.mp.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Around("execution(* com.help.mp.controller..*.*(..))")
    public Object logOperation(ProceedingJoinPoint pjp) throws Throwable {
        String clazz = pjp.getTarget().getClass().getSimpleName();
        String method = ((MethodSignature) pjp.getSignature()).getMethod().getName();
        Long userId = UserContext.getUserId();
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            log.info("[OP] {}#{} userId={} cost={}ms", clazz, method, userId, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable e) {
            log.warn("[OP] {}#{} userId={} error={}", clazz, method, userId, e.getMessage());
            throw e;
        }
    }
}
