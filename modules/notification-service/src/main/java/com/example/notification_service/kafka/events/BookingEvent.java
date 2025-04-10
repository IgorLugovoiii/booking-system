package com.example.notification_service.kafka.events;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingEvent {
    private String eventType;
    private Long bookingId;
    private Long userId;
    private Long itemId;
    private LocalDateTime bookingTime;
}
