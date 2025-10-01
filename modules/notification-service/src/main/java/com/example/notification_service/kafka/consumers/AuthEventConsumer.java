package com.example.notification_service.kafka.consumers;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.kafka.events.UserEvent;
import com.example.notification_service.services.api.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuthEventConsumer extends BaseEventConsumer<UserEvent> {
    public AuthEventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        super(objectMapper, notificationService);
    }

    @KafkaListener(topics = "${spring.kafka.topics.auth}", groupId = "notification-group")
    public void consume(String message) throws JsonProcessingException {
        super.consume(message);
    }

    @Override
    protected Class<UserEvent> getEventClass() {
        return UserEvent.class;
    }

    @Override
    protected NotificationRequest buildNotification(UserEvent event) {
        String subject = switch (event.getEventType()) {
            case "user.registered" -> "User successfully registered";
            case "user.role.updated" -> "User role updated";
            case "user.deleted" -> "User deleted";
            default -> "User event, something is wrong";
        };

        String msg = "User with id " + event.getUserId() +
                " and username " + event.getUsername() +
                " -> " + event.getEventType();

        return new NotificationRequest("qeadzc4065@gmail.com", subject, msg);
    }
}
