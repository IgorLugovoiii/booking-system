package com.example.notification_service.kafka;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.services.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ItemEventConsumer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NotificationService notificationService;

    @Autowired
    public ItemEventConsumer(NotificationService notificationService){
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "item-events", groupId = "notification-service-group")
    public void consumeItemCreatedEvent(String message){
        try {
            ItemEvent itemEvent = objectMapper.readValue(message, ItemEvent.class);

            NotificationRequest request = new NotificationRequest();
            request.setTo("recipient@example.com"); // замінити і зробити для зацікавлених юзерів
            request.setSubject("Подія: " + itemEvent.getEventType());
            request.setMessage("Товар: " + itemEvent.getName() + "\nОпис: " + itemEvent.getDescription());

            notificationService.sendNotification(request);

            System.out.println("✔ Email sent for event: " + itemEvent.getEventType());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendNotification(ItemEvent itemEvent) {
        // додати логіку
        System.out.println("Sending notification for item: " + itemEvent.getName());
    }
}
