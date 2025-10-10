package com.example.payment_service.services.strategy;

import com.example.payment_service.dtos.PaymentRequest;
import com.example.payment_service.dtos.PaymentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface PaymentStrategy {
    PaymentResponse pay(PaymentRequest paymentRequest) throws JsonProcessingException;
}
