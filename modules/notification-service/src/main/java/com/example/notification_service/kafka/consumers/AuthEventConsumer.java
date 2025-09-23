package com.example.notification_service.kafka.consumers;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.exception.KafkaMessageReceiveException;
import com.example.notification_service.kafka.events.UserEvent;
import com.example.notification_service.services.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthEventConsumer {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "auth-events", groupId = "notification-group")
    public void handleAuthEvents(String message) {
        try {
            UserEvent userEvent = objectMapper.readValue(message, UserEvent.class);

            String subject = switch (userEvent.getEventType()) {
                case "user.registered" -> "User successfully registered";
                case "user.role.updated" -> "User role is updated";
                case "user.deleted" -> "User successfully deleted";
                default -> "Message about user";
            };
            String msg = "User with id #" + userEvent.getUserId() + " and username #" + userEvent.getUsername() +
                    " created/role updated/deleted";

            NotificationRequest request = new NotificationRequest("qeadzc4065@gmail.com",subject,msg);//пошта заглушка

            notificationService.sendNotification(request);

            System.out.println("User event processed: " + userEvent.getEventType());

        } catch (Exception e) {
            throw new KafkaMessageReceiveException("Failed to receive user event" , e);
        }
    }


}
