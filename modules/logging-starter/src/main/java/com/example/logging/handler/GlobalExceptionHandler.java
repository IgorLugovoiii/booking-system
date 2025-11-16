package com.example.logging.handler;

import com.example.logging.producer.LogProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final LogProducer logProducer;

    @Value("${service.name:unknown-service}")
    private String serviceName;

    public ResponseEntity<Map<String, Object>> buildResponse(Exception ex, HttpStatus status) throws JsonProcessingException {
        String traceId = MDC.get("traceId");
        traceId = (traceId != null) ? traceId : "N/A";

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now(),
                "traceId", traceId,
                "service", serviceName,
                "message", ex.getMessage(),
                "exception", ex.getClass().getSimpleName(),
                "status", status.value()
        );

        logProducer.sendLogEvent(body);

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) throws JsonProcessingException {
        return buildResponse(ex, HttpStatus.BAD_REQUEST); // 400
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(Exception ex) throws JsonProcessingException {
        return buildResponse(ex, HttpStatus.FORBIDDEN); // 403
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(Exception ex) throws JsonProcessingException {
        return buildResponse(ex, HttpStatus.UNAUTHORIZED); // 401
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) throws JsonProcessingException {
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }
}
