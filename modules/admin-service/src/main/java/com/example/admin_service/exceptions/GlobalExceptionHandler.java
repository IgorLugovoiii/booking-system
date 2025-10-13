package com.example.admin_service.exceptions;

import com.example.admin_service.kafka.LogProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final LogProducer logProducer;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) throws JsonProcessingException {
        String traceId = MDC.get("traceId");
        traceId = (traceId != null) ? traceId : "N/A";

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now(),
                "traceId", traceId,
                "message", ex.getMessage(),
                "exception", ex.getClass().getSimpleName(),
                "stackTrace", Arrays.toString(ex.getStackTrace()),
                "status", 500
        );

        logProducer.sendLogEvent(body);

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}