package com.example.notification_service.kafka;

import lombok.Data;

@Data
public class UserEvent {
    private Long userId;
    private String eventType;
    private String username;
    private String role;
}
