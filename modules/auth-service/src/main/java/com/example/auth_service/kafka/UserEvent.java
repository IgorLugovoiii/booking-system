package com.example.auth_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserEvent {
    private Long userId;
    private String eventType;
    private String username;
    private String role;
}
