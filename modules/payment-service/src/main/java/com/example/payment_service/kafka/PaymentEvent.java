package com.example.payment_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentEvent {
    private String eventType;
    private Long paymentId;
    private Long bookingId;
    private Long userId;
    private Double amount;
    private LocalDateTime paymentDate;
}
