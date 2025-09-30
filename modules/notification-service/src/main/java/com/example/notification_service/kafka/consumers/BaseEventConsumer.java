package com.example.notification_service.kafka.consumers;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.services.api.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;

@RequiredArgsConstructor
public abstract class BaseEventConsumer<T> {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "#{__listener.topic()}", groupId = "notification-group") //__listener — це системна назва, зарезервована у Spring Kafka, #{...} означає: «обчисли вираз у дужках як SpEL»
    public void consume(String message) throws JsonProcessingException {
        T event = objectMapper.readValue(message, getEventClass());
        NotificationRequest request = buildNotification(event);
        notificationService.sendNotification(request);
    }

    protected abstract String topic();
    protected abstract Class<T> getEventClass();
    protected abstract NotificationRequest buildNotification(T event);
}
