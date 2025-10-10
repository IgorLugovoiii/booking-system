package com.example.payment_service.services.impl;

import com.example.payment_service.dtos.PaymentRequest;
import com.example.payment_service.dtos.PaymentResponse;
import com.example.payment_service.kafka.PaymentProducer;
import com.example.payment_service.services.api.PaymentService;
import com.example.payment_service.services.strategy.PaymentStrategy;
import com.example.payment_service.services.strategy.PaymentStrategyFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentStrategyFactory strategyFactory;
    private final PaymentProducer paymentProducer;

    @Transactional
    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) throws JsonProcessingException {
        PaymentStrategy strategy = strategyFactory.getStrategy(paymentRequest.getPaymentType());
        return strategy.pay(paymentRequest);
    }


}
