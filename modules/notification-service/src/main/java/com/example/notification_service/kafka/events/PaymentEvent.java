package com.example.notification_service.kafka.events;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {
    private String eventType;
    private Long paymentId;
    private Long bookingId;
    private Long userId;
    private Double amount;
    private LocalDateTime paymentDate;
}
