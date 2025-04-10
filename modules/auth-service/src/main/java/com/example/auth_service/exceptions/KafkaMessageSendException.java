package com.example.auth_service.exceptions;

public class KafkaMessageSendException extends RuntimeException{
    public KafkaMessageSendException(String message, Throwable ex){
        super(message, ex);
    }
}
