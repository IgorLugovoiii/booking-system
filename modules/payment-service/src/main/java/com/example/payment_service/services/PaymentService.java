package com.example.payment_service.services;

import com.example.payment_service.dtos.PaymentRequest;
import com.example.payment_service.dtos.PaymentResponse;
import com.example.payment_service.kafka.PaymentEvent;
import com.example.payment_service.kafka.PaymentProducer;
import com.example.payment_service.models.Payment;
import com.example.payment_service.models.enums.PaymentStatus;
import com.example.payment_service.repositories.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;

    @CircuitBreaker(name = "paymentService")
    @Retry(name = "paymentService")
    @RateLimiter(name = "paymentService")
    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) throws JsonProcessingException {
        Payment payment = new Payment();
        payment.setBookingId(paymentRequest.getBookingId());
        payment.setUserId(paymentRequest.getUserId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        Payment saved = paymentRepository.save(payment);

        paymentProducer.sendPaymentEvent(new PaymentEvent(
                "payment.success",
                saved.getId(),
                saved.getBookingId(),
                saved.getUserId(),
                saved.getAmount(),
                saved.getPaymentDate()
        ));

        return new PaymentResponse(saved.getId(), saved.getPaymentStatus(), saved.getPaymentDate());
    }
}
