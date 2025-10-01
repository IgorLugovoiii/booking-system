package com.example.notification_service.kafka.consumers;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.services.api.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseEventConsumer<T> {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public void consume(String message) throws JsonProcessingException {
        T event = objectMapper.readValue(message, getEventClass());
        NotificationRequest request = buildNotification(event);
        notificationService.sendNotification(request);
    }

    protected abstract Class<T> getEventClass();

    protected abstract NotificationRequest buildNotification(T event);
}
