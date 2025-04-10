package com.example.notification_service.kafka.consumers;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.exception.KafkaMessageReceiveException;
import com.example.notification_service.kafka.events.UserEvent;
import com.example.notification_service.services.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuthEventConsumer {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Autowired
    public AuthEventConsumer(ObjectMapper objectMapper, NotificationService notificationService){
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "auth-events", groupId = "notification-group")
    public void handleAuthEvents(String message) {
        try {
            UserEvent userEvent = objectMapper.readValue(message, UserEvent.class);

            String subject = switch (userEvent.getEventType()) {
                case "user.registered" -> "Юзер успішно зареєстрований";
                case "user.role.updated" -> "Роль юзера успішно оновлена";
                case "user.deleted" -> "Юзер успішно видалений";
                default -> "Повідомлення про юзера";
            };
            String msg = "Юзер з id #" + userEvent.getUserId() + " і з username #" + userEvent.getUsername() +
                    " створено/оновлено роль/видалено";

            NotificationRequest request = new NotificationRequest("qeadzc4065@gmail.com",subject,msg);//пошта заглушка

            notificationService.sendNotification(request);

            System.out.println("User event оброблено: " + userEvent.getEventType());

        } catch (Exception e) {
            throw new KafkaMessageReceiveException("Failed to receive user event" , e);
        }
    }


}
