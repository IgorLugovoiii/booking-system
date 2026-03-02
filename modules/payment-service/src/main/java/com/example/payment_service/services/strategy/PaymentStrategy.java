package com.example.payment_service.services.strategy;

import com.example.payment_service.dtos.PaymentRequest;
import com.example.payment_service.dtos.PaymentResponse;

public interface PaymentStrategy {
    PaymentResponse pay(PaymentRequest paymentRequest);
}
