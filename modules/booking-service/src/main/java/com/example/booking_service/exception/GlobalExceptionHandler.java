package com.example.booking_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public ResponseEntity<Map<String, Object>> handleAll(Exception ex){
        String traceId = MDC.get("traceId");
        log.error("Unhandled exception, traceId={}", traceId, ex);
        Map<String, Object> body = Map.of(
                "timestamp", Instant.now(),
                "traceId", traceId,
                "message", ex.getMessage(),
                "exception", ex.getClass().getSimpleName(),
                "stackTrace", Arrays.toString(ex.getStackTrace()),
                "status", 500
        );

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
