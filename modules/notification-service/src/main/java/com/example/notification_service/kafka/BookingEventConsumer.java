package com.example.notification_service.kafka;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.services.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BookingEventConsumer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NotificationService notificationService;

    @Autowired
    public BookingEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "booking-events", groupId = "notification-service-group")
    public void consumeBookingEvent(String message) {
        try {
            BookingEvent event = objectMapper.readValue(message, BookingEvent.class);

            String subject = switch (event.getEventType()) {
                case "booking.created" -> "Ваше бронювання створено!";
                case "booking.updated" -> "Бронювання оновлено";
                case "booking.canceled" -> "Бронювання скасовано";
                case "booking.confirmed" -> "Бронювання підтверджено";
                default -> "Повідомлення про бронювання";
            };
            String msg = "Бронювання #" + event.getBookingId() + " для item #" + event.getItemId() +
                    " створено/оновлено/скасовано в: " + event.getBookingTime();

            NotificationRequest request = new NotificationRequest();
            request.setTo("recipient@example.com"); // Поки заглушка
            request.setSubject(subject);
            request.setMessage(msg);

            notificationService.sendNotification(request);

            System.out.println("Booking event оброблено: " + event.getEventType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
