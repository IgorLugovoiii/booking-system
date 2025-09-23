package com.example.notification_service.kafka.consumers;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.exception.KafkaMessageReceiveException;
import com.example.notification_service.kafka.events.BookingEvent;
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
                case "booking.created" -> "Booking created";
                case "booking.updated" -> "Booking updated";
                case "booking.canceled" -> "Booking canceled";
                case "booking.confirmed" -> "Booking confirmed";
                default -> "Message about booking";
            };
            String msg = "Booking #" + event.getBookingId() + " for item #" + event.getItemId() +
                    " created/updated/deleted at: " + event.getBookingTime();

            NotificationRequest request = new NotificationRequest("qeadzc4065@gmail.com", subject, msg);// заглушка
            notificationService.sendNotification(request);

            System.out.println("Booking event processed: " + event.getEventType());
        } catch (Exception e) {
            throw new KafkaMessageReceiveException("Failed to receive booking event" , e);
        }
    }
}
