package com.example.logging.aspect;

import com.example.logging.producer.LogProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {
    private final LogProducer logProducer;

    @Value("${service.name:unknown-service}")
    private String serviceName;

    @Around("execution(* com.example..*(..)) && !within(com.example.logging..*)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        String traceId = MDC.get("traceId");
        String userId = MDC.get("userId");

        traceId = traceId != null ? traceId : "N/A";
        userId = userId != null ? userId : "anonymous";

        Object result;

        Map<String, Object> logMap = new HashMap<>();
        logMap.put("timestamp", Instant.now());
        logMap.put("service", serviceName);
        logMap.put("traceId", traceId);
        logMap.put("userId", userId);
        logMap.put("method", method);

        try {
            result = joinPoint.proceed();
            logMap.put("level", "INFO");
            logMap.put("message", "Method executed successfully");
            logMap.put("resultType", result != null ? result.getClass().getSimpleName() : "null");
        } catch (Throwable ex) {
            logMap.put("level", "ERROR");
            logMap.put("message", ex.getMessage());
            logMap.put("exception", ex.toString());
            throw ex;
        }
        logProducer.sendLogEvent(logMap);
        return result;
    }
}
