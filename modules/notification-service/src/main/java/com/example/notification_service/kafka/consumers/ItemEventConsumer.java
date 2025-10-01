package com.example.notification_service.kafka.consumers;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.kafka.events.ItemEvent;
import com.example.notification_service.services.api.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ItemEventConsumer extends BaseEventConsumer<ItemEvent> {
    public ItemEventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        super(objectMapper, notificationService);
    }

    @KafkaListener(topics = "${spring.kafka.topics.item}", groupId = "notification-group")
    public void consume(String message) throws JsonProcessingException {
        super.consume(message);
    }

    @Override
    protected Class<ItemEvent> getEventClass() {
        return ItemEvent.class;
    }

    @Override
    protected NotificationRequest buildNotification(ItemEvent event) {
        String subject = switch (event.getEventType()) {
            case "item.created" -> "Item successfully created";
            case "item.updated" -> "Item updated";
            case "item.deleted" -> "Item deleted";
            default -> "Item event, something is wrong";
        };

        String msg = "Event: " + event.getEventType() + " for item with id: "
                + event.getItemId() + " name: " + event.getName() + " and price: " + event.getPrice();

        return new NotificationRequest("qeadzc4065@gmail.com", subject, msg);
    }
}
