package com.markglow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 控制器统一请求日志切面：记录路径、入参、出参、耗时
 */
@Aspect
@Component
@Slf4j
public class RequestLoggingAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("execution(public * com.markglow.controller..*(..))")
    public Object logController(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String method = signature.getDeclaringType().getSimpleName() + "." + signature.getName();

        Map<String, Object> argsMap = new HashMap<>();
        String[] paramNames = signature.getParameterNames();
        Object[] args = pjp.getArgs();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length && i < args.length; i++) {
                argsMap.put(paramNames[i], args[i]);
            }
        }

        try {
            log.info("API start: {} args={}", method, objectMapper.writeValueAsString(argsMap));
            Object result = pjp.proceed();
            long cost = System.currentTimeMillis() - start;
            log.info("API end: {} cost={}ms", method, cost);
            return result;
        } catch (Throwable ex) {
            long cost = System.currentTimeMillis() - start;
            log.error("API error: {} cost={}ms ex={}", method, cost, ex.getMessage(), ex);
            throw ex;
        }
    }
}

