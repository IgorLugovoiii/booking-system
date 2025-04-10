package com.example.notification_service.kafka.events;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentEvent {
    private String eventType;
    private Long paymentId;
    private Long bookingId;
    private Long userId;
    private Double amount;
    private LocalDateTime paymentDate;
}
