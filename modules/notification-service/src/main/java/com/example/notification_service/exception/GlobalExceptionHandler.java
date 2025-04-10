package com.example.notification_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex){
        ErrorResponse errorResponse = new ErrorResponse("Illegal state", LocalDateTime.now(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception ex){
        ErrorResponse errorResponse = new ErrorResponse("Unexpected error", LocalDateTime.now(), ex.getMessage());
        return  new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(KafkaMessageReceiveException.class)
    public ResponseEntity<ErrorResponse> handleKafkaMessageSend(Exception ex){
        ErrorResponse errorResponse = new ErrorResponse("Unexpected error while sending message by Kafka", LocalDateTime.now(), ex.getMessage());
        return  new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
