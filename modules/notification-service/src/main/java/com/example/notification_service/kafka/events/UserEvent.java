package com.example.notification_service.kafka.events;

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
