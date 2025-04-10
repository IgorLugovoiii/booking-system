package com.example.notification_service.dtos;

import lombok.Data;

@Data
public class NotificationRequest {
    private String to;
    private String subject;
    private String message;

    public NotificationRequest(String to, String subject, String message){
        this.to = to;
        this.subject = subject;
        this.message = message;
    }
}
