package com.example.booking_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingEvent {
    private String eventType;
    private Long bookingId;
    private Long userId;
    private Long itemId;
    private LocalDateTime bookingTime;
}
