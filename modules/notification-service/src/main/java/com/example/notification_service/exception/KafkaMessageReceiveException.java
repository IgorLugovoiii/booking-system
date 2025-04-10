package com.example.notification_service.exception;

public class KafkaMessageReceiveException extends RuntimeException{
    public KafkaMessageReceiveException(String message, Throwable ex){
        super(message, ex);
    }
}
