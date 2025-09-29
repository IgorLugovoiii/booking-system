package com.example.notification_service.services.api;

import com.example.notification_service.dtos.NotificationRequest;

public interface NotificationService {
    void sendNotification(NotificationRequest notification);
}
