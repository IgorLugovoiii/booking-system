package com.example.inventory_service.exception;

public class KafkaMessageSendException extends RuntimeException{
    public KafkaMessageSendException(String message, Throwable ex){
        super(message, ex);
    }
}
