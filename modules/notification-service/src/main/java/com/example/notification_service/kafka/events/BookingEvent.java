package com.example.notification_service.kafka.events;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEvent {
    private String eventType;
    private Long bookingId;
    private Long userId;
    private Long itemId;
    private LocalDateTime bookingTime;
}
