package com.example.payment_service.kafka;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentEvent {
    private String eventType;
    private Long paymentId;
    private Long bookingId;
    private Long userId;
    private Double amount;
    private LocalDateTime paymentDate;
}
