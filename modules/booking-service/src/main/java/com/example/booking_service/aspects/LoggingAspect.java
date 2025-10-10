package com.example.booking_service.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.example.booking_service..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        String traceId = MDC.get("traceId");
        String userId = MDC.get("userId");

        Object result;
        try {
            result = joinPoint.proceed();
            log.info("{}", Map.of(
                    "timestamp", Instant.now(),
                    "service", "booking-service",
                    "level", "INFO",
                    "traceId", traceId,
                    "userId", userId,
                    "method", method,
                    "message", "Method executed successfully",
                    "result", result
            ));
        } catch (Throwable ex) {
            log.error("{}", Map.of(
                    "timestamp", Instant.now(),
                    "service", "booking-service",
                    "level", "ERROR",
                    "traceId", traceId,
                    "userId", userId,
                    "method", method,
                    "message", ex.getMessage(),
                    "exception", ex
            ));
            throw ex;
        }
        return result;
    }
}
