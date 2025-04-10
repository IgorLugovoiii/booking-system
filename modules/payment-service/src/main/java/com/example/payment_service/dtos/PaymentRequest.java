package com.example.payment_service.dtos;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long bookingId;
    private Long userId;
    private Double amount;
}
