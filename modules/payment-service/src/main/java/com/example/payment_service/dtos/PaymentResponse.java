package com.example.payment_service.dtos;

import com.example.payment_service.models.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
}
