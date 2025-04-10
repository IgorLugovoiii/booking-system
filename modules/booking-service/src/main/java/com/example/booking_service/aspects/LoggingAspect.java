package com.example.booking_service.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Pointcut("execution(* com.example.booking_service.services..*(..)) || execution(* com.example.booking_service.kafka..*(..))")
    public void appMethods() {} // не справжній метод, іменований маркер, для того, щоб не прописували у кожному методі

    @Before("appMethods()")
    public void logBefore(JoinPoint joinPoint){
        log.info("Method: {}, args: {}", joinPoint.getSignature(), joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "appMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result){
        log.info("Completed method: {}, with result: {}", joinPoint.getSignature(), result);
    }

    @AfterThrowing(pointcut = "appMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex){
        log.error("Exception in: {}, message: {}", joinPoint.getSignature(), ex.getMessage(), ex);
    }

}
