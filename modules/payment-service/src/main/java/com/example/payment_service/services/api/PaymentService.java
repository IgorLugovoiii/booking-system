package com.example.payment_service.services.api;

import com.example.payment_service.dtos.PaymentRequest;
import com.example.payment_service.dtos.PaymentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest paymentRequest) throws JsonProcessingException;
}
