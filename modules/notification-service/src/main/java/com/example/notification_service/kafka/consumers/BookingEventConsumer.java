package com.example.notification_service.kafka.consumers;

import com.example.notification_service.dtos.NotificationRequest;
import com.example.notification_service.kafka.events.BookingEvent;
import com.example.notification_service.services.api.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class BookingEventConsumer extends BaseEventConsumer<BookingEvent> {
    public BookingEventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        super(objectMapper, notificationService);
    }

    @Override
    protected String topic() {
        return "booking-events";
    }

    @Override
    protected Class<BookingEvent> getEventClass() {
        return BookingEvent.class;
    }

    @Override
    protected NotificationRequest buildNotification(BookingEvent event) {
        String subject = switch (event.getEventType()) {
            case "booking.created" -> "Booking created";
            case "booking.updated" -> "Booking updated";
            case "booking.canceled" -> "Booking canceled";
            case "booking.confirmed" -> "Booking confirmed";
            default -> "Message about booking, something is wrong";
        };

        String msg = "Booking " + event.getBookingId() + " for item " + event.getItemId() +
                " " + event.getEventType() +" " + event.getBookingTime();

        return new NotificationRequest("qeadzc4065@gmail.com", subject, msg);
    }
}
