package com.example.auth_service.kafka;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent {
    private Long userId;
    private String eventType;
    private String username;
    private String role;
}
